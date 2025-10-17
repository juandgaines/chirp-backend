-- IP RATE LIMITING LUA SCRIPT
-- ============================
-- This script implements a sliding window rate limiter for IP addresses.
-- It's executed atomically in Redis to prevent race conditions.
--
-- PURPOSE:
-- Tracks and limits the number of requests from a specific IP address
-- within a time window to prevent abuse, DDoS attacks, and brute force attempts.
--
-- HOW IT WORKS:
-- 1. Each IP gets a counter in Redis with a TTL (time-to-live)
-- 2. First request creates the counter and sets expiry
-- 3. Subsequent requests increment the counter
-- 4. When limit is reached, further requests are rejected
-- 5. Counter auto-expires after TTL, resetting the limit

-- Input parameters from Spring application
local key = KEYS[1]                    -- Redis key: "rate_limit:ip:192.168.1.100"
local max_requests = tonumber(ARGV[1]) -- Maximum allowed requests (e.g., 10)
local ttl = tonumber(ARGV[2])          -- Time window in seconds (e.g., 60)

-- Check if this IP already has a counter in Redis
local current = redis.call("GET", key)

if current == false then
    -- FIRST REQUEST FROM THIS IP
    -- Create new counter with value 1 and set expiry time
    redis.call("SET", key, 1, "EX", ttl)
    return {1, ttl}  -- Return: {current_count=1, time_until_reset=ttl}
else
    -- SUBSEQUENT REQUESTS FROM THIS IP
    local count = tonumber(current)
    local remaining_ttl = redis.call("TTL", key)  -- Get remaining time until reset
    
    if count < max_requests then
        -- UNDER LIMIT: Allow request and increment counter
        local new_count = redis.call("INCR", key)
        return {new_count, remaining_ttl}  -- Return: {updated_count, time_until_reset}
    else
        -- LIMIT REACHED: Reject request but still track attempt
        -- Note: We return count+1 to show the actual attempt count
        -- but don't update Redis (to avoid extending the TTL)
        return {count+1, remaining_ttl}  -- Return: {exceeded_count, time_until_reset}
    end
end

-- EXAMPLE EXECUTION FLOW:
-- =======================
-- Assuming max_requests=3, ttl=60 seconds
--
-- Time 0s:   IP 192.168.1.100 makes request #1
--            → Redis: SET "rate_limit:ip:192.168.1.100" = 1 (expires in 60s)
--            → Returns: {1, 60} - ALLOWED
--
-- Time 10s:  Same IP makes request #2
--            → Redis: INCR "rate_limit:ip:192.168.1.100" = 2
--            → Returns: {2, 50} - ALLOWED
--
-- Time 20s:  Same IP makes request #3
--            → Redis: INCR "rate_limit:ip:192.168.1.100" = 3
--            → Returns: {3, 40} - ALLOWED (at limit)
--
-- Time 25s:  Same IP makes request #4
--            → Redis: No increment (limit reached)
--            → Returns: {4, 35} - REJECTED (429 Too Many Requests)
--
-- Time 30s:  Same IP makes request #5
--            → Redis: No increment (still over limit)
--            → Returns: {4, 30} - REJECTED
--
-- Time 60s:  Redis key expires automatically
--
-- Time 61s:  Same IP makes new request
--            → Redis: SET "rate_limit:ip:192.168.1.100" = 1 (new window)
--            → Returns: {1, 60} - ALLOWED (reset!)

-- WHY USE LUA?
-- ============
-- 1. ATOMIC EXECUTION: All operations run as single transaction
-- 2. NO RACE CONDITIONS: Multiple requests can't interfere
-- 3. PERFORMANCE: Runs directly in Redis, no network round-trips
-- 4. CONSISTENCY: GET-check-SET happens atomically
