package com.example.busnotice.util;

import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class RecentSearchManager {

    private static final String RECENT_SEARCH_KEY = "recent_busStopName";
    private static final int MAX_ITEMS = 5;

    private final StringRedisTemplate redisTemplate;
    private final ZSetOperations<String, String> zSetOperations;

    public RecentSearchManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOperations = redisTemplate.opsForZSet();
    }

    // 검색어 추가
    public void addSearchTerm(String term) {
        long currentTime = System.currentTimeMillis();

        // 1. 검색어 추가 (동일한 검색어면 최신 시간으로 갱신됨)
        zSetOperations.add(RECENT_SEARCH_KEY, term, currentTime);

        // 2. 검색어 개수 제한 (5개 초과 시 가장 오래된 항목 삭제)
        Long size = zSetOperations.zCard(RECENT_SEARCH_KEY);
        if (size != null && size > MAX_ITEMS) {
            // 오래된 검색어 삭제
            zSetOperations.removeRange(RECENT_SEARCH_KEY, 0, size - MAX_ITEMS - 1);
        }
    }

    // 최근 검색어 목록 조회 (최신순)
    public Set<String> getRecentSearches() {
        return zSetOperations.reverseRange(RECENT_SEARCH_KEY, 0, MAX_ITEMS - 1);
    }
}
