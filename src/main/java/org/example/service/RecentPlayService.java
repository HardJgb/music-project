package org.example.service;

import org.example.pojo.RecentPlay;
import java.util.List;

public interface RecentPlayService {
    void addRecentPlay(Integer userId, Integer songId);
    List<RecentPlay> getRecentPlays(Integer userId, Integer limit);
    void deleteRecentPlay(Integer userId, Integer songId);
    void clearRecentPlays(Integer userId);
} 