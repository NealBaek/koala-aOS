package com.ksdigtalnomad.koala.ui.customView.calendarView.month;

import com.ksdigtalnomad.koala.ui.customView.calendarView.day.DayModel;

import java.util.ArrayList;

public class MonthModel{

    public ArrayList<DayModel> dayList;
    public int month;
    public int year;
    public int numberOfDaysInTheMonth;
    public int index;
//    statistics? (구체화 필요)

    public MonthModel clone(){
        //내 객체 생성
        Object toReturn;
        try {
            toReturn = super.clone();
            return ((MonthModel)toReturn);
        }
        catch (CloneNotSupportedException e) { return this; }
    }
}
