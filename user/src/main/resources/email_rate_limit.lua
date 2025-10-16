-- Get the rate limit key (used to track if user is currently rate-limited)
local rateLimitKey = KEYS[1]
-- Get the attempt count key (used to track total number of attempts)
local attemptCountKey = KEYS[2]

-- Check if the user is currently rate-limited
if redis.call("EXISTS", rateLimitKey) == 1 then
    -- If rate-limited, get the remaining time-to-live (TTL) in seconds
    local ttl = redis.call("TTL", rateLimitKey)
    -- Return -1 (indicating rate limit active) and the remaining wait time
    return {-1, ttl>0 and ttl or 60}
end

-- Get the current attempt count from Redis
local currentCount = redis.call("GET", attemptCountKey)
-- Convert to number or default to 0 if key doesn't exist
currentCount = currentCount and tonumber(currentCount) or 0

-- Increment the attempt counter and get the new value
local newCount = redis.call("INCR", attemptCountKey)

-- Define exponential backoff periods in seconds: 1 min, 5 min, 1 hour
local backOffSeconds = { 60, 300, 3600}
-- Calculate backoff index (1-3) based on current attempt count, capped at 3
local backOffIndex = math.min(currentCount, 2) + 1

-- Set the rate limit key with expiration based on backoff period
redis.call('SETEX', rateLimitKey, backOffSeconds[backOffIndex],1)
-- Set the attempt counter to expire after 24 hours (86400 seconds)
redis.call('EXPIRE', attemptCountKey, 86400)

-- Return the new attempt count and 0 (no wait time since not rate-limited)
return {newCount,0}

