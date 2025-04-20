package com.softwareengineering.finsage.controllers;

import com.softwareengineering.finsage.dao.HolidayDao;
import com.softwareengineering.finsage.model.Holiday;
import com.softwareengineering.finsage.utils.UserLoginState;

import java.util.List;

public class HolidayController {
    private HolidayDao holidayDao;

    public HolidayController() {
        this.holidayDao = new HolidayDao();
    }

    public List<Holiday> getHolidays() {
        return holidayDao.getByUserId(UserLoginState.getCurrentUserId());
    }

    public boolean addHoliday(Holiday holiday) {
        if (holidayDao.existsByNameAndUserId(holiday.getName(), holiday.getUserId())) {
            return false;
        }

        holiday.setId(java.util.UUID.randomUUID().toString());
        holidayDao.save(holiday);
        return true;
    }

    public boolean deleteHoliday(String holidayId) {
        return holidayDao.delete(holidayId);
    }
}