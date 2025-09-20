package com.lawding.leavecalc.service;

import com.lawding.leavecalc.repository.DailyUserJdbcRepository;
import java.time.LocalDate;

public class DailyUserService {

    private final DailyUserJdbcRepository dailyUserRepository;

    // 생성자 주입
    public DailyUserService(DailyUserJdbcRepository dailyUserRepository) {
        this.dailyUserRepository = dailyUserRepository;
    }
    public void recordUser(String platform, boolean testMode){
        if(!testMode){
            dailyUserRepository.incrementCount(LocalDate.now(),platform);
        }
    }
}
