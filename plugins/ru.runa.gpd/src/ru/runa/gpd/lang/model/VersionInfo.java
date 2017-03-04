package ru.runa.gpd.lang.model;

import java.util.Calendar;
import java.util.Date;

import ru.runa.wfe.commons.CalendarUtil;

public class VersionInfo {
    private Calendar date = Calendar.getInstance();
    private String author = "";
    private String comment = "";
    private boolean savedToFile = false;

    public VersionInfo() {
    }

    public VersionInfo(String dateTimeAsString, String author, String comment) {
        this.date = CalendarUtil.convertToCalendar(dateTimeAsString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
        this.author = author;
        this.comment = comment;
    }

    public Calendar getDate() {
        return date;
    }

    public String getDateAsString() {
        return CalendarUtil.format(date, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
    }

    public String getDateTimeAsString() {
        return CalendarUtil.format(date, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setDate(String dateAsString) {
        this.date.setTime(CalendarUtil.convertToDate(dateAsString, CalendarUtil.DATE_WITHOUT_TIME_FORMAT));
    }

    public void setDateTime(Date dateTime) {
        this.date = CalendarUtil.dateToCalendar(dateTime);
    }

    public void setDateTime(String dateTimeAsString) {
        this.date.setTime(CalendarUtil.convertToDate(dateTimeAsString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT));
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setSavedToFile(boolean savedToFile) {
        this.savedToFile = savedToFile;
    }

    public boolean isSavedToFile() {
        return savedToFile;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if ((other instanceof VersionInfo) != true) {
            return false;
        }

        VersionInfo versionInfo = (VersionInfo) other;
        if (versionInfo.getDateTimeAsString().equals(this.getDateTimeAsString()) && versionInfo.getAuthor().equals(this.getAuthor())
                && versionInfo.getComment().equals(this.getComment())) {
            return true;
        }

        return false;

    }

    @Override
    public int hashCode() {
        return this.getDateTimeAsString().concat(this.getAuthor()).concat(this.getComment()).hashCode();
    }
}