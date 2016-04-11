package ru.runa.xpdl.convertor;

import java.io.BufferedWriter;

/**
 * Created by IntelliJ IDEA. User: Администратор Date: 04.05.2012 Time: 17:57:01
 */
public class EmptyCDataWriter extends BufferedWriter {

    private short status = 0;

    /*
     * 0 -start 1- after & 2-after &l 12 after &g 3-after &lt 13-after &gt
     */
    public EmptyCDataWriter(java.io.Writer writer) {
        super(writer);
    }

    public EmptyCDataWriter(java.io.Writer writer, int i) {
        super(writer, i);
    }

    @Override
    public void write(int i) throws java.io.IOException {
        switch (status) {
        case 0:
            if (i == '&') {
                status = 1;
                return;
            }
            break;
        case 1:
            if (i == 'l') {
                status = 2;
                return;
            }
            if (i == 'g') {
                status = 12;
                return;
            }
            status = 0;
            super.write('&');
            break;
        case 2:
            if (i == 't') {
                status = 3;
                return;
            }
            status = 0;
            super.write('&');
            super.write('l');
            break;
        case 12:
            if (i == 't') {
                status = 13;
                return;
            }
            status = 0;
            super.write('&');
            super.write('g');
            break;
        case 3:
            if (i == ';') {
                status = 0;
                super.write('<');
                return;
            }
            status = 0;
            super.write('&');
            super.write('l');
            super.write('t');
            break;
        case 13:
            if (i == ';') {
                status = 0;
                super.write('>');
                return;
            }
            status = 0;
            super.write('&');
            super.write('l');
            super.write('g');
            break;
        }
        super.write(i);
    }

    @Override
    public void write(char[] chars, int i, int i1) throws java.io.IOException {
        for (int k = 0; (k < i1) && (i + k < chars.length); k++) {
            write(chars[i + k]);
        }
    }

    @Override
    public void write(java.lang.String s, int i, int i1) throws java.io.IOException {
        for (int k = 0; (k < i1) && (i + k < s.length()); k++) {
            write(s.charAt(i + k));
        }

    }
}
