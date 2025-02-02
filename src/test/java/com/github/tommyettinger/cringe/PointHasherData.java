package com.github.tommyettinger.cringe;

import java.math.BigInteger;

public class PointHasherData {
    public static void showNextPrime(String name, String num) {
        System.out.printf("%s * %s ^ ", name, "0x" + BigInteger.valueOf(Integer.parseInt(num, 16) - 1).nextProbablePrime().toString(16).toUpperCase());
    }

    public static void showConstant(String name, int dim, String num) {
        System.out.printf("%s = %s, ", name.toUpperCase() + dim, "0x" + BigInteger.valueOf(Integer.parseInt(num, 16) - 1).nextProbablePrime().toString(16).toUpperCase());
    }

    public static void main(String[] args) {
        System.out.println("public static final int");
        for (String x : new String[]{
                "x * 0x1827F5 ^ y * 0x123C21",
                "x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725",
                "x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31",
                "x * 0x1C3361 ^ y * 0x18DA39 ^ z * 0x15E6DB ^ w * 0x134D29 ^ u * 0x110281",
                "x * 0x1CC1C5 ^ y * 0x19D7AF ^ z * 0x173935 ^ w * 0x14DEAF ^ u * 0x12C139 ^ v * 0x10DAA3",
        }) {
            String[] pairs = x.split(" \\^ ");
            int dim = pairs.length;
            for (String pair : pairs) {
                String name = pair.substring(0, 1), num = pair.substring(6);
                showConstant(name, dim, num);
            }
            System.out.println();
        }
    }
}
/*
x * 0x1827F5 ^ y * 0x123C3B ^
x * 0x1A36BF ^ y * 0x157931 ^ z * 0x119749 ^
x * 0x1B69E5 ^ y * 0x177C1F ^ z * 0x141E75 ^ w * 0x113C33 ^
x * 0x1C3367 ^ y * 0x18DA39 ^ z * 0x15E6E3 ^ w * 0x134D49 ^ u * 0x110281 ^
x * 0x1CC205 ^ y * 0x19D7B5 ^ z * 0x173935 ^ w * 0x14DEC5 ^ u * 0x12C139 ^ v * 0x10DAAD ^
 */
/*
public static final int
X2 = 0x1827F5, Y2 = 0x123C3B,
X3 = 0x1A36BF, Y3 = 0x157931, Z3 = 0x119749,
X4 = 0x1B69E5, Y4 = 0x177C1F, Z4 = 0x141E75, W4 = 0x113C33,
X5 = 0x1C3367, Y5 = 0x18DA39, Z5 = 0x15E6E3, W5 = 0x134D49, U5 = 0x110281,
X6 = 0x1CC205, Y6 = 0x19D7B5, Z6 = 0x173935, W6 = 0x14DEC5, U6 = 0x12C139, V6 = 0x10DAAD;
 */