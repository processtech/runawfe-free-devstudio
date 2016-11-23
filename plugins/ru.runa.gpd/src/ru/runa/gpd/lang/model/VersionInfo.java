package ru.runa.gpd.lang.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VersionInfo {
    private String number = "";
    private Calendar date = Calendar.getInstance();
    private String author = "";
    private String comment = "";
    private boolean savedToFile = false;
    final static private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public VersionInfo() {
    }

    public VersionInfo(String number, String dateAsString, String author, String comment) {
        this.number = number;
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(simpleDateFormat.parse(dateAsString));
        } catch (ParseException e) {

        }
        this.date = cal;
        this.author = author;
        this.comment = comment;
    }

    static public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Calendar getDate() {
        return date;
    }

    public String getDateAsString() {
        return getSimpleDateFormat().format(date.getTime());
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setDate(String dateAsString) {
        try {
            this.date.setTime(getSimpleDateFormat().parse(dateAsString));
        } catch (ParseException e) {

        }
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
        if (versionInfo.getNumber().equals(this.getNumber()) && versionInfo.getDateAsString().equals(this.getDateAsString())
                && versionInfo.getAuthor().equals(this.getAuthor()) && versionInfo.getComment().equals(this.getComment())) {
            return true;
        }

        return false;

    }

    @Override
    public int hashCode() {
        return this.getNumber().concat(this.getDateAsString()).concat(this.getAuthor()).concat(this.getComment()).hashCode();
    }
}