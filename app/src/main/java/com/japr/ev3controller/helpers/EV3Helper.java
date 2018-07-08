package com.japr.ev3controller.helpers;

public class EV3Helper {

    private static final char DIRECT_COMMAND_REPLY = 0x00;
    private static final char DIRECT_COMMAND_NO_REPLY = 0x80;
    private static final char MESSAGE_COUNTER = 0x0016;

    public enum Motor {
        A,
        B,
        C,
        D
    }

    private static String getMessageCounter() {
        char firstByte = (char) (MESSAGE_COUNTER & 0xFF00);
        char secondByte = (char) (MESSAGE_COUNTER & 0x00FF);
        return new String(new char[]{secondByte, firstByte});
    }

    private static String calculateLength(String packet) {
        short length = (short) packet.length();
        char firstByte = (char) (length & 0xFF00);
        char secondByte = (char) (length & 0x00FF);
        return new String(new char[]{secondByte, firstByte});
    }

    private static String getEmptyHeader() {
        return new String(new char[]{(char)0x00, (char)0x00});
    }

    public static String startMotorCommand(Motor[] motors, char[] speeds) {
        if (motors.length != speeds.length)
            return "";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getMessageCounter());
        stringBuilder.append(DIRECT_COMMAND_NO_REPLY);
        stringBuilder.append(getEmptyHeader());

        char motorValues = 0;

        for (int i = 0; i < motors.length; i++) {
            Motor motor = motors[i];
            char motorValue = 0;
            switch (motor) {
                case A:
                    motorValue += 0x01;
                    break;
                case B:
                    motorValue += 0x02;
                    break;
                case C:
                    motorValue += 0x04;
                    break;
                case D:
                    motorValue += 0x08;
                    break;
            }
            motorValues += motorValue;

            stringBuilder.append((char) 0xA5);
            stringBuilder.append((char) 0x00);
            stringBuilder.append(motorValue);
            stringBuilder.append((char) 0x81);

            char speed = speeds[i];
            stringBuilder.append(speed);
        }

        stringBuilder.append((char)0xA6);
        stringBuilder.append((char)0x00);
        stringBuilder.append(motorValues);
        String length = calculateLength(stringBuilder.toString());
        stringBuilder.insert(0, length);
        return stringBuilder.toString();
    }

    public static String stopMotors() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getMessageCounter());
        stringBuilder.append(DIRECT_COMMAND_NO_REPLY);
        stringBuilder.append(getEmptyHeader());

        stringBuilder.append((char)0xA3);
        stringBuilder.append((char)0x00);
        stringBuilder.append((char)0x0F);
        stringBuilder.append((char)0x00);
        String length = calculateLength(stringBuilder.toString());
        stringBuilder.insert(0, length);
        return stringBuilder.toString();
    }



}
