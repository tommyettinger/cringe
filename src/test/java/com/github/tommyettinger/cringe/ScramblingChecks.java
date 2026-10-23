package com.github.tommyettinger.cringe;

public class ScramblingChecks {
    public static void mainLong(String[] args){
        long idx = 0x31000000000L;
        for (int i = 0; i < 2048; i++) {
            for (int j = 0; j < 0x40000000; j++) {
                if(idx == Scramblers.scramble(idx))
                    System.err.printf("\n\n 0x%016X is a fixed-point.\n\n", idx);
                idx++;
            }
            System.out.print((i+1) + "/2048... ");
        }
        System.out.printf("\n\n\nGot through 0x%XL indices.\n", idx);
    }

    /**
     * Checking the AEMD technique on MX3: 0x130000000000L or 20890720927744 indices without a fixed-point.
     * Over 20 trillion, but nowhere near exhaustive.
     *
     * @param args
     */
    public static void mainLongAEMD(String[] args){
        long idx = 0x130000000000L;
        for (int i = 0; i < 16384; i++) {
            for (int j = 0; j < 0x40000000; j++) {
                if(idx == scramble(idx - 1))
                    System.err.printf("\n\n 0x%016X is a fixed-point.\n\n", idx);
                idx++;
            }
            System.out.print((i+1) + "/16384... ");
        }
        System.out.printf("\n\n\nGot through 0x%XL indices.\n", idx);
    }
    public static long scramble(long x) {
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 29;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        return x ^ x >>> 29;
    }

    public static int scrambleIntMx3Like(int x) {
        x ^= x >>> 16;
        x = Compatibility.imul(x, 0xEB34556D);
        x ^= x >>> 13;
        x = Compatibility.imul(x, 0xEB34556D);
        x ^= x >>> 16;
        x = Compatibility.imul(x, 0xEB34556D);
        x ^= x >>> 13;
        return x;
    }

    public static int scrambleInt(int x) {
        x ^= x >>> 16;
        x = Compatibility.imul(x, 0x236F7153);
        x ^= x >>> 12;
        x = Compatibility.imul(x, 0x33CD8663);
        x ^= x >>> 15;
        x = Compatibility.imul(x, 0x3E06B66B);
        x ^= x >>> 16;
        return x;
    }

    public static int badScramble7(int x) {
        x ^= x >>> 1;
        x = Compatibility.imul(x, 3);
        x ^= x >>> 1;
        x = Compatibility.imul(x, 3);
        x ^= x >>> 1;
        x = Compatibility.imul(x, 3);
        x ^= x >>> 1;
        return x;
    }

    /**
     * This is an involution, which may explain why it doesn't work with AEMD.
     * @param x
     * @return
     */
    public static int badScramble7b(int x) {
        x ^= x >>> 31;
        x = -x;
        x ^= x >>> 31;
        x = -x;
        x ^= x >>> 31;
        x = -x;
        x ^= x >>> 31;
        return x;
    }

    public static int badScramble7c(int x) {
        x ^= x >>> 1;
        x = -x;
        x ^= x >>> 31;
        x = -x;
        x ^= x >>> 1;
        x = -x;
        x ^= x >>> 31;
        return x;
    }

    /**
     * Not an involution, but every one of its component steps is.
     * @param x
     * @return
     */
    public static int badScramble7d(int x) {
        x ^= x >>> 19;
        x = -x;
        x ^= x >>> 31;
        x = -x;
        x ^= x >>> 19;
        x = -x;
        x ^= x >>> 31;
        return x;
    }

    /**
     * Shifts are involutions, but the multiplications aren't (all by 3).
     *
     * @param x
     * @return
     */
    public static int badScramble7e(int x) {
        x ^= x >>> 19;
        x *= 3;
        x ^= x >>> 31;
        x *= 3;
        x ^= x >>> 19;
        x *= 3;
        x ^= x >>> 31;
        return x;
    }

    public static int badScramble7f(int x) {
        x ^= x >>> 19;
        x *= 5;
        x ^= x >>> 31;
        x *= 5;
        x ^= x >>> 19;
        x *= 5;
        x ^= x >>> 31;
        return x;
    }

    public static int badScramble5(int x) {
        x ^= x >>> 1;
        x = Compatibility.imul(x, 3);
        x ^= x >>> 1;
        x = Compatibility.imul(x, 3);
        x ^= x >>> 1;
        return x;
    }

    /**
     * Uses three different shift amounts, but always multiplies by 3.
     *
     * @param x
     * @return
     */
    public static int badScramble5b(int x) {
        x ^= x >>> 1;
        x = Compatibility.imul(x, 3);
        x ^= x >>> 2;
        x = Compatibility.imul(x, 3);
        x ^= x >>> 3;
        return x;
    }

    public static int leaScramble(int x) {
        x ^= x >>> 16;
        x *= 0xb09322e3;
        x ^= x >>> 16;
        x *= 0xb09322e3;
        x ^= x >>> 16;
        return x;
    }

    public static int badScramble3(int x) {
        x ^= x >>> 1;
        x = Compatibility.imul(x, 3);
        x ^= x >>> 1;
        return x;
    }

    public static int badScramble3b(int x) {
        x ^= x >>> 1;
        x = Compatibility.imul(x, 3);
        x ^= x >>> 3;
        return x;
    }

    /**
     * 0x8329E33B is a fixed-point for a=1.
     * 0x17FD53DC is a fixed-point for a=2.
     * 0xDAC33353 is a fixed-point for a=2.
     * 0x37B8EFA7 is a fixed-point for a=4.
     * 0x976BF754 is a fixed-point for a=4.
     * 0xE4F04311 is a fixed-point for a=4.
     * 0x4428DE41 is a fixed-point for a=5.
     * 0x45AE2C1B is a fixed-point for a=5.
     * 0x8B3C89E3 is a fixed-point for a=6.
     * 0xA152A2DF is a fixed-point for a=7.
     * 0xFDEEACEC is a fixed-point for a=7.
     * 0x05DC4BBB is a fixed-point for a=12.
     * 0x29A00EC3 is a fixed-point for a=12.
     * 0x10E6F189 is a fixed-point for a=13.
     * @param args ignored
     */
    public static void mainAdding(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == scrambleInt(idx + a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * 0xD43A4C34 is a fixed-point for a=2.
     * 0x9384DF05 is a fixed-point for a=4.
     * 0x681470DC is a fixed-point for a=5.
     * 0x00ED5055 is a fixed-point for a=6.
     * 0x8C7BDA61 is a fixed-point for a=7.
     * 0x813DE4A9 is a fixed-point for a=11.
     * 0xFE70814B is a fixed-point for a=13.
     * 0x7E7264C5 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainMinus(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == scrambleInt(idx - a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * 0xEF301BF2 is a fixed-point for a=1.
     * 0x757F9AF1 is a fixed-point for a=3.
     * 0x2A9A344C is a fixed-point for a=4.
     * 0x7C6962A7 is a fixed-point for a=4.
     * 0xA5942275 is a fixed-point for a=7.
     * 0xAC82C53C is a fixed-point for a=7.
     * 0x34F36FAB is a fixed-point for a=9.
     * 0xFDFC851A is a fixed-point for a=10.
     * 0x78C5789A is a fixed-point for a=13.
     * 0x9B1DE494 is a fixed-point for a=13.
     * 0x66940A6F is a fixed-point for a=14.
     * 0x823A2A83 is a fixed-point for a=14.
     *
     * @param args
     */
    public static void mainReverseMinus(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == scrambleInt(a - idx))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Testing the xor-encrypt-xor system with the same xor value on both sides:
     * 0x00000001 is a fixed-point for a=1.
     * 0x54F4B636 is a fixed-point for a=1.
     * 0xA26E8047 is a fixed-point for a=1.
     * 0x00000002 is a fixed-point for a=2.
     * 0x54F4B635 is a fixed-point for a=2.
     * 0xA26E8044 is a fixed-point for a=2.
     * 0x00000003 is a fixed-point for a=3.
     * 0x54F4B634 is a fixed-point for a=3.
     * 0xA26E8045 is a fixed-point for a=3.
     * 0x00000004 is a fixed-point for a=4.
     * 0x54F4B633 is a fixed-point for a=4.
     * 0xA26E8042 is a fixed-point for a=4.
     * 0x00000005 is a fixed-point for a=5.
     * 0x54F4B632 is a fixed-point for a=5.
     * 0xA26E8043 is a fixed-point for a=5.
     * 0x00000006 is a fixed-point for a=6.
     * 0x54F4B631 is a fixed-point for a=6.
     * 0xA26E8040 is a fixed-point for a=6.
     * 0x00000007 is a fixed-point for a=7.
     * 0x54F4B630 is a fixed-point for a=7.
     * 0xA26E8041 is a fixed-point for a=7.
     * 0x00000008 is a fixed-point for a=8.
     * 0x54F4B63F is a fixed-point for a=8.
     * 0xA26E804E is a fixed-point for a=8.
     * 0x00000009 is a fixed-point for a=9.
     * 0x54F4B63E is a fixed-point for a=9.
     * 0xA26E804F is a fixed-point for a=9.
     * 0x0000000A is a fixed-point for a=10.
     * 0x54F4B63D is a fixed-point for a=10.
     * 0xA26E804C is a fixed-point for a=10.
     * 0x0000000B is a fixed-point for a=11.
     * 0x54F4B63C is a fixed-point for a=11.
     * 0xA26E804D is a fixed-point for a=11.
     * 0x0000000C is a fixed-point for a=12.
     * 0x54F4B63B is a fixed-point for a=12.
     * 0xA26E804A is a fixed-point for a=12.
     * 0x0000000D is a fixed-point for a=13.
     * 0x54F4B63A is a fixed-point for a=13.
     * 0xA26E804B is a fixed-point for a=13.
     * 0x0000000E is a fixed-point for a=14.
     * 0x54F4B639 is a fixed-point for a=14.
     * 0xA26E8048 is a fixed-point for a=14.
     * 0x0000000F is a fixed-point for a=15.
     * 0x54F4B638 is a fixed-point for a=15.
     * 0xA26E8049 is a fixed-point for a=15.
     * <br>
     * Exactly 3 fixed-points for each, with patterns for each!
     *
     * @param args
     */
    public static void mainXEX(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx ^ a) ^ a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Add-encrypt-add has fewer and less-predictable fixed-points than XEX.
     * Some values for {@code a} have no fixed-points.
     * <br>
     * 0x17FD53DD is a fixed-point for a=1.
     * 0xDAC33354 is a fixed-point for a=1.
     * 0x37B8EFA9 is a fixed-point for a=2.
     * 0x976BF756 is a fixed-point for a=2.
     * 0xE4F04313 is a fixed-point for a=2.
     * 0x8B3C89E6 is a fixed-point for a=3.
     * 0x05DC4BC1 is a fixed-point for a=6.
     * 0x29A00EC9 is a fixed-point for a=6.
     * 0x61C43937 is a fixed-point for a=8.
     * 0x44394305 is a fixed-point for a=10.
     * 0xB9B81C49 is a fixed-point for a=10.
     * 0xD401EE73 is a fixed-point for a=10.
     * 0x792F1D68 is a fixed-point for a=12.
     * 0xE753463C is a fixed-point for a=12.
     * 0x79248A69 is a fixed-point for a=13.
     * 0x8A06FD30 is a fixed-point for a=13.
     *
     * @param args
     */
    public static void mainAEA(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx + a) + a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Add-encrypt-minus (not calling it subtract, since the acronym would be AES) has similar traits to XEX.
     * <br>
     * 0x54F4B636 is a fixed-point for a=1.
     * 0xA26E8045 is a fixed-point for a=1.
     * 0xFFFFFFFF is a fixed-point for a=1.
     * 0x54F4B635 is a fixed-point for a=2.
     * 0xA26E8044 is a fixed-point for a=2.
     * 0xFFFFFFFE is a fixed-point for a=2.
     * 0x54F4B634 is a fixed-point for a=3.
     * 0xA26E8043 is a fixed-point for a=3.
     * 0xFFFFFFFD is a fixed-point for a=3.
     * 0x54F4B633 is a fixed-point for a=4.
     * 0xA26E8042 is a fixed-point for a=4.
     * 0xFFFFFFFC is a fixed-point for a=4.
     * 0x54F4B632 is a fixed-point for a=5.
     * 0xA26E8041 is a fixed-point for a=5.
     * 0xFFFFFFFB is a fixed-point for a=5.
     * 0x54F4B631 is a fixed-point for a=6.
     * 0xA26E8040 is a fixed-point for a=6.
     * 0xFFFFFFFA is a fixed-point for a=6.
     * 0x54F4B630 is a fixed-point for a=7.
     * 0xA26E803F is a fixed-point for a=7.
     * 0xFFFFFFF9 is a fixed-point for a=7.
     * 0x54F4B62F is a fixed-point for a=8.
     * 0xA26E803E is a fixed-point for a=8.
     * 0xFFFFFFF8 is a fixed-point for a=8.
     * 0x54F4B62E is a fixed-point for a=9.
     * 0xA26E803D is a fixed-point for a=9.
     * 0xFFFFFFF7 is a fixed-point for a=9.
     * 0x54F4B62D is a fixed-point for a=10.
     * 0xA26E803C is a fixed-point for a=10.
     * 0xFFFFFFF6 is a fixed-point for a=10.
     * 0x54F4B62C is a fixed-point for a=11.
     * 0xA26E803B is a fixed-point for a=11.
     * 0xFFFFFFF5 is a fixed-point for a=11.
     * 0x54F4B62B is a fixed-point for a=12.
     * 0xA26E803A is a fixed-point for a=12.
     * 0xFFFFFFF4 is a fixed-point for a=12.
     * 0x54F4B62A is a fixed-point for a=13.
     * 0xA26E8039 is a fixed-point for a=13.
     * 0xFFFFFFF3 is a fixed-point for a=13.
     * 0x54F4B629 is a fixed-point for a=14.
     * 0xA26E8038 is a fixed-point for a=14.
     * 0xFFFFFFF2 is a fixed-point for a=14.
     * 0x54F4B628 is a fixed-point for a=15.
     * 0xA26E8037 is a fixed-point for a=15.
     * 0xFFFFFFF1 is a fixed-point for a=15.
     * <br>
     * Exactly 3 fixed-points for each, with patterns for each!
     *
     * @param args
     */
    public static void mainAEM(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx + a) - a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Add-encrypt-minus-decrement.
     * <br>
     * NO FIXED-POINTS! Can also use {@code (encrypt(idx + a) + ~a)}
     * <br>
     * Importantly for this function, just decrementing idx is enough to ensure it has no fixed-points.
     * I'm not sure if this works for all encrypt() functions...
     * <br>
     * Checked 2304 somewhat-random values for {@code a}, and none had fixed-points.
     *
     * @param args
     */
    public static void mainAEMD(String[] args) {
        int idx = 0, a = 0x9E3779B9 * 16 * (16 + 128);
        for (int b = 0; b < 16; b++) {
            for (int c = 0; c < 128; c++) {


//        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
                a += 0x9E3779B9;
                for (int i = 0; i < 16; i++) {
                    for (int j = 0; j < 0x10000000; j++) {
                        if (idx == (scrambleInt(idx + a) - a - 1))
                            System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                        idx++;
                    }
                }
            }
            System.out.println("Completed " + (b + 1) + " of 16.");
        }
    }

    /**
     * AEMD does NOT avoid fixed-points when using MX3's style of 7-line interleave of shifts by 16, multiplications by
     * one constant, and shifts by 13. This likely carries over to long math on shifts by 32 and 29.
     * <br>
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleIntMx3Like(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * A different encrypt method still has no fixed-points when pre-adding {@code a} and post-adding {@code ~a}.
     *
     * @param args
     */
    public static void mainBad7(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble7(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * There are fixed-points for this scramble with AEMD! They follow a pattern!
     * <br>
     * 0xD9B364F3 is a fixed-point for a=-16.
     * 0xD9B366D9 is a fixed-point for a=-16.
     * 0xD9B364F2 is a fixed-point for a=-15.
     * 0xD9B366D8 is a fixed-point for a=-15.
     * 0xD9B364F1 is a fixed-point for a=-14.
     * 0xD9B366D7 is a fixed-point for a=-14.
     * 0xD9B364F0 is a fixed-point for a=-13.
     * 0xD9B366D6 is a fixed-point for a=-13.
     * 0xD9B364EF is a fixed-point for a=-12.
     * 0xD9B366D5 is a fixed-point for a=-12.
     * 0xD9B364EE is a fixed-point for a=-11.
     * 0xD9B366D4 is a fixed-point for a=-11.
     * 0xD9B364ED is a fixed-point for a=-10.
     * 0xD9B366D3 is a fixed-point for a=-10.
     * 0xD9B364EC is a fixed-point for a=-9.
     * 0xD9B366D2 is a fixed-point for a=-9.
     * 0xD9B364EB is a fixed-point for a=-8.
     * 0xD9B366D1 is a fixed-point for a=-8.
     * 0xD9B364EA is a fixed-point for a=-7.
     * 0xD9B366D0 is a fixed-point for a=-7.
     * 0xD9B364E9 is a fixed-point for a=-6.
     * 0xD9B366CF is a fixed-point for a=-6.
     * 0xD9B364E8 is a fixed-point for a=-5.
     * 0xD9B366CE is a fixed-point for a=-5.
     * 0xD9B364E7 is a fixed-point for a=-4.
     * 0xD9B366CD is a fixed-point for a=-4.
     * 0xD9B364E6 is a fixed-point for a=-3.
     * 0xD9B366CC is a fixed-point for a=-3.
     * 0xD9B364E5 is a fixed-point for a=-2.
     * 0xD9B366CB is a fixed-point for a=-2.
     * 0xD9B364E4 is a fixed-point for a=-1.
     * 0xD9B366CA is a fixed-point for a=-1.
     * 0xD9B364E3 is a fixed-point for a=0.
     * 0xD9B366C9 is a fixed-point for a=0.
     * 0xD9B364E2 is a fixed-point for a=1.
     * 0xD9B366C8 is a fixed-point for a=1.
     * 0xD9B364E1 is a fixed-point for a=2.
     * 0xD9B366C7 is a fixed-point for a=2.
     * 0xD9B364E0 is a fixed-point for a=3.
     * 0xD9B366C6 is a fixed-point for a=3.
     * 0xD9B364DF is a fixed-point for a=4.
     * 0xD9B366C5 is a fixed-point for a=4.
     * 0xD9B364DE is a fixed-point for a=5.
     * 0xD9B366C4 is a fixed-point for a=5.
     * 0xD9B364DD is a fixed-point for a=6.
     * 0xD9B366C3 is a fixed-point for a=6.
     * 0xD9B364DC is a fixed-point for a=7.
     * 0xD9B366C2 is a fixed-point for a=7.
     * 0xD9B364DB is a fixed-point for a=8.
     * 0xD9B366C1 is a fixed-point for a=8.
     * 0xD9B364DA is a fixed-point for a=9.
     * 0xD9B366C0 is a fixed-point for a=9.
     * 0xD9B364D9 is a fixed-point for a=10.
     * 0xD9B366BF is a fixed-point for a=10.
     * 0xD9B364D8 is a fixed-point for a=11.
     * 0xD9B366BE is a fixed-point for a=11.
     * 0xD9B364D7 is a fixed-point for a=12.
     * 0xD9B366BD is a fixed-point for a=12.
     * 0xD9B364D6 is a fixed-point for a=13.
     * 0xD9B366BC is a fixed-point for a=13.
     * 0xD9B364D5 is a fixed-point for a=14.
     * 0xD9B366BB is a fixed-point for a=14.
     * 0xD9B364D4 is a fixed-point for a=15.
     * 0xD9B366BA is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainBad5(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble5(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * It isn't the number of operations in the scrambler, but if the shifts are all different, AEMD works.
     * No fixed-points.
     *
     * @param args
     */
    public static void mainBad5b(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble5b(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * 0x00000000 is a fixed-point for the Lea-like scramble.
     * 0x87455BF2 is a fixed-point for the Lea-like scramble.
     * 0xDF559521 is a fixed-point for the Lea-like scramble.
     * @param args
     */
    public static void mainLeaLike(String[] args) {
        int idx = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 0x10000000; j++) {
                if (idx == (leaScramble(idx)))
                    System.err.printf("0x%08X is a fixed-point for the Lea-like scramble.\n", idx);
                idx++;
            }
        }
    }

    /**
     * No fixed-points for an AEMD approach on the Lea-like scramble.
     *
     * @param args
     */
    public static void mainLeaLikeAEMD(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (leaScramble(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Fewer fixed-points, but following an obvious pattern here. AEMD with a 3-line scramble.
     * <br>
     * 0x00000011 is a fixed-point for a=-16.
     * 0x00000010 is a fixed-point for a=-15.
     * 0x0000000F is a fixed-point for a=-14.
     * 0x0000000E is a fixed-point for a=-13.
     * 0x0000000D is a fixed-point for a=-12.
     * 0x0000000C is a fixed-point for a=-11.
     * 0x0000000B is a fixed-point for a=-10.
     * 0x0000000A is a fixed-point for a=-9.
     * 0x00000009 is a fixed-point for a=-8.
     * 0x00000008 is a fixed-point for a=-7.
     * 0x00000007 is a fixed-point for a=-6.
     * 0x00000006 is a fixed-point for a=-5.
     * 0x00000005 is a fixed-point for a=-4.
     * 0x00000004 is a fixed-point for a=-3.
     * 0x00000003 is a fixed-point for a=-2.
     * 0x00000002 is a fixed-point for a=-1.
     * 0x00000001 is a fixed-point for a=0.
     * 0x00000000 is a fixed-point for a=1.
     * 0xFFFFFFFF is a fixed-point for a=2.
     * 0xFFFFFFFE is a fixed-point for a=3.
     * 0xFFFFFFFD is a fixed-point for a=4.
     * 0xFFFFFFFC is a fixed-point for a=5.
     * 0xFFFFFFFB is a fixed-point for a=6.
     * 0xFFFFFFFA is a fixed-point for a=7.
     * 0xFFFFFFF9 is a fixed-point for a=8.
     * 0xFFFFFFF8 is a fixed-point for a=9.
     * 0xFFFFFFF7 is a fixed-point for a=10.
     * 0xFFFFFFF6 is a fixed-point for a=11.
     * 0xFFFFFFF5 is a fixed-point for a=12.
     * 0xFFFFFFF4 is a fixed-point for a=13.
     * 0xFFFFFFF3 is a fixed-point for a=14.
     * 0xFFFFFFF2 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainBad3(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble3(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * A simple 3-line scramble with two different small xorshifts and a multiply in-between works with AEMD!
     * No fixed-points.
     *
     * @param args
     */
    public static void mainBad3b(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble3b(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * AEMD with the worst 7-line scrambler I could make; this has a fixed-point per {@code a}.
     * The scrambler only uses -1 as a multiplier and 31 as a shift.
     * <br>
     * 0x80000010 is a fixed-point for a=-16.
     * 0x8000000F is a fixed-point for a=-15.
     * 0x8000000E is a fixed-point for a=-14.
     * 0x8000000D is a fixed-point for a=-13.
     * 0x8000000C is a fixed-point for a=-12.
     * 0x8000000B is a fixed-point for a=-11.
     * 0x8000000A is a fixed-point for a=-10.
     * 0x80000009 is a fixed-point for a=-9.
     * 0x80000008 is a fixed-point for a=-8.
     * 0x80000007 is a fixed-point for a=-7.
     * 0x80000006 is a fixed-point for a=-6.
     * 0x80000005 is a fixed-point for a=-5.
     * 0x80000004 is a fixed-point for a=-4.
     * 0x80000003 is a fixed-point for a=-3.
     * 0x80000002 is a fixed-point for a=-2.
     * 0x80000001 is a fixed-point for a=-1.
     * 0x80000000 is a fixed-point for a=0.
     * 0x7FFFFFFF is a fixed-point for a=1.
     * 0x7FFFFFFE is a fixed-point for a=2.
     * 0x7FFFFFFD is a fixed-point for a=3.
     * 0x7FFFFFFC is a fixed-point for a=4.
     * 0x7FFFFFFB is a fixed-point for a=5.
     * 0x7FFFFFFA is a fixed-point for a=6.
     * 0x7FFFFFF9 is a fixed-point for a=7.
     * 0x7FFFFFF8 is a fixed-point for a=8.
     * 0x7FFFFFF7 is a fixed-point for a=9.
     * 0x7FFFFFF6 is a fixed-point for a=10.
     * 0x7FFFFFF5 is a fixed-point for a=11.
     * 0x7FFFFFF4 is a fixed-point for a=12.
     * 0x7FFFFFF3 is a fixed-point for a=13.
     * 0x7FFFFFF2 is a fixed-point for a=14.
     * 0x7FFFFFF1 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainBad7b(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble7b(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * 7-line (bad) scramble method, but this one isn't an involution. AEMD, no fixed-points.
     * {@code badScramble7c} mixes shifts of 1 and 31, and all its multiplies are by -1.
     *
     * @param args
     */
    public static void mainBad7c(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble7c(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * 7-line bad scramble method, with each line an involution but the whole thing not; AEMD.
     * <br>
     * 0x80000010 is a fixed-point for a=-16.
     * 0x80001010 is a fixed-point for a=-16.
     * 0x8000000F is a fixed-point for a=-15.
     * 0x8000100F is a fixed-point for a=-15.
     * 0x8000000E is a fixed-point for a=-14.
     * 0x8000100E is a fixed-point for a=-14.
     * 0x8000000D is a fixed-point for a=-13.
     * 0x8000100D is a fixed-point for a=-13.
     * 0x8000000C is a fixed-point for a=-12.
     * 0x8000100C is a fixed-point for a=-12.
     * 0x8000000B is a fixed-point for a=-11.
     * 0x8000100B is a fixed-point for a=-11.
     * 0x8000000A is a fixed-point for a=-10.
     * 0x8000100A is a fixed-point for a=-10.
     * 0x80000009 is a fixed-point for a=-9.
     * 0x80001009 is a fixed-point for a=-9.
     * 0x80000008 is a fixed-point for a=-8.
     * 0x80001008 is a fixed-point for a=-8.
     * 0x80000007 is a fixed-point for a=-7.
     * 0x80001007 is a fixed-point for a=-7.
     * 0x80000006 is a fixed-point for a=-6.
     * 0x80001006 is a fixed-point for a=-6.
     * 0x80000005 is a fixed-point for a=-5.
     * 0x80001005 is a fixed-point for a=-5.
     * 0x80000004 is a fixed-point for a=-4.
     * 0x80001004 is a fixed-point for a=-4.
     * 0x80000003 is a fixed-point for a=-3.
     * 0x80001003 is a fixed-point for a=-3.
     * 0x80000002 is a fixed-point for a=-2.
     * 0x80001002 is a fixed-point for a=-2.
     * 0x80000001 is a fixed-point for a=-1.
     * 0x80001001 is a fixed-point for a=-1.
     * 0x80000000 is a fixed-point for a=0.
     * 0x80001000 is a fixed-point for a=0.
     * 0x7FFFFFFF is a fixed-point for a=1.
     * 0x80000FFF is a fixed-point for a=1.
     * 0x7FFFFFFE is a fixed-point for a=2.
     * 0x80000FFE is a fixed-point for a=2.
     * 0x7FFFFFFD is a fixed-point for a=3.
     * 0x80000FFD is a fixed-point for a=3.
     * 0x7FFFFFFC is a fixed-point for a=4.
     * 0x80000FFC is a fixed-point for a=4.
     * 0x7FFFFFFB is a fixed-point for a=5.
     * 0x80000FFB is a fixed-point for a=5.
     * 0x7FFFFFFA is a fixed-point for a=6.
     * 0x80000FFA is a fixed-point for a=6.
     * 0x7FFFFFF9 is a fixed-point for a=7.
     * 0x80000FF9 is a fixed-point for a=7.
     * 0x7FFFFFF8 is a fixed-point for a=8.
     * 0x80000FF8 is a fixed-point for a=8.
     * 0x7FFFFFF7 is a fixed-point for a=9.
     * 0x80000FF7 is a fixed-point for a=9.
     * 0x7FFFFFF6 is a fixed-point for a=10.
     * 0x80000FF6 is a fixed-point for a=10.
     * 0x7FFFFFF5 is a fixed-point for a=11.
     * 0x80000FF5 is a fixed-point for a=11.
     * 0x7FFFFFF4 is a fixed-point for a=12.
     * 0x80000FF4 is a fixed-point for a=12.
     * 0x7FFFFFF3 is a fixed-point for a=13.
     * 0x80000FF3 is a fixed-point for a=13.
     * 0x7FFFFFF2 is a fixed-point for a=14.
     * 0x80000FF2 is a fixed-point for a=14.
     * 0x7FFFFFF1 is a fixed-point for a=15.
     * 0x80000FF1 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainBad7d(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble7d(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * No fixed-points! This uses a "bad" scramble that isn't an involution (it multiplies by 3, not -1). AEMD.
     *
     * @param args
     */
    public static void mainBad7e(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble7e(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Multiplying by 3 and by 5 both work equally well with AEMD scrambles; no fixed-points.
     *
     * @param args
     */
    public static void mainBad7f(String[] args) {
        int idx = 0;
        for (int a = -16; a < 16; a++) {
//        for (int a = 1; a != 0; a<<=1) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (badScramble7f(idx + a) - a - 1))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * XOR-Encrypt-(NOT-XOR) does not work very well.
     * <br>
     * 0x80BE62A8 is a fixed-point for a=0.
     * 0xE4CDBEF3 is a fixed-point for a=0.
     * 0x80BE62A9 is a fixed-point for a=1.
     * 0xE4CDBEF2 is a fixed-point for a=1.
     * 0x80BE62AA is a fixed-point for a=2.
     * 0xE4CDBEF1 is a fixed-point for a=2.
     * 0x80BE62AB is a fixed-point for a=3.
     * 0xE4CDBEF0 is a fixed-point for a=3.
     * 0x80BE62AC is a fixed-point for a=4.
     * 0xE4CDBEF7 is a fixed-point for a=4.
     * 0x80BE62AD is a fixed-point for a=5.
     * 0xE4CDBEF6 is a fixed-point for a=5.
     * 0x80BE62AE is a fixed-point for a=6.
     * 0xE4CDBEF5 is a fixed-point for a=6.
     * 0x80BE62AF is a fixed-point for a=7.
     * 0xE4CDBEF4 is a fixed-point for a=7.
     * 0x80BE62A0 is a fixed-point for a=8.
     * 0xE4CDBEFB is a fixed-point for a=8.
     * 0x80BE62A1 is a fixed-point for a=9.
     * 0xE4CDBEFA is a fixed-point for a=9.
     * 0x80BE62A2 is a fixed-point for a=10.
     * 0xE4CDBEF9 is a fixed-point for a=10.
     * 0x80BE62A3 is a fixed-point for a=11.
     * 0xE4CDBEF8 is a fixed-point for a=11.
     * 0x80BE62A4 is a fixed-point for a=12.
     * 0xE4CDBEFF is a fixed-point for a=12.
     * 0x80BE62A5 is a fixed-point for a=13.
     * 0xE4CDBEFE is a fixed-point for a=13.
     * 0x80BE62A6 is a fixed-point for a=14.
     * 0xE4CDBEFD is a fixed-point for a=14.
     * 0x80BE62A7 is a fixed-point for a=15.
     * 0xE4CDBEFC is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainXENX(String[] args){
        int idx = 0;
        for (int a = 0; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx ^ a) ^ ~a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Add-Encrypt-(NOT-XOR) has very strange results, with some patterns but not reliable ones.
     * <br>
     * 0x80BE62A8 is a fixed-point for a=0.
     * 0xE4CDBEF3 is a fixed-point for a=0.
     * 0xE4CDBEF2 is a fixed-point for a=1.
     * 0xE4CDBEF1 is a fixed-point for a=2.
     * 0xE4CDBEF0 is a fixed-point for a=3.
     * 0x537D3AC7 is a fixed-point for a=4.
     * 0x5A6BDD8E is a fixed-point for a=4.
     * 0x537D3AC6 is a fixed-point for a=5.
     * 0x537D3AC5 is a fixed-point for a=6.
     * 0x5A6BDD8C is a fixed-point for a=6.
     * 0x537D3AC4 is a fixed-point for a=7.
     * 0x80BE62A0 is a fixed-point for a=8.
     * 0x074F513F is a fixed-point for a=9.
     * 0x074F513D is a fixed-point for a=11.
     * 0x5A6BDD86 is a fixed-point for a=12.
     * 0x074F513B is a fixed-point for a=13.
     * 0x5A6BDD84 is a fixed-point for a=14.
     * 0x074F5139 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainAENX(String[] args){
        int idx = 0;
        for (int a = 0; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx + a) ^ ~a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Minus-decrement-encrypt-XOR.
     * <br>
     * 0x8AC4B1C0 is a fixed-point for a=9.
     * 0x9BF24130 is a fixed-point for a=9.
     * 0xB2F6B700 is a fixed-point for a=9.
     * 0x3A69BBC1 is a fixed-point for a=10.
     * 0xF1B88A25 is a fixed-point for a=10.
     * 0x8AC4B1C2 is a fixed-point for a=11.
     * 0x9BF24132 is a fixed-point for a=11.
     * 0xB2F6B702 is a fixed-point for a=11.
     * 0x8AC4B1C4 is a fixed-point for a=13.
     * 0x9BF24134 is a fixed-point for a=13.
     * 0xB2F6B704 is a fixed-point for a=13.
     * 0x3A69BBC5 is a fixed-point for a=14.
     * 0x8AC4B1C6 is a fixed-point for a=15.
     * 0x9BF24136 is a fixed-point for a=15.
     * 0xB2F6B706 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainMDEX(String[] args){
        int idx = 0;
        for (int a = 0; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx - a - 1) ^ a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Xor-encrypt-add has predictable fixed-points at {@code a} and sometimes more.
     * <br>
     * 0x00000001 is a fixed-point for a=1.
     * 0xA26E8047 is a fixed-point for a=1.
     * 0xDAC33354 is a fixed-point for a=1.
     * 0x00000002 is a fixed-point for a=2.
     * 0x37B8EFA9 is a fixed-point for a=2.
     * 0x00000003 is a fixed-point for a=3.
     * 0xDAC33356 is a fixed-point for a=3.
     * 0x00000004 is a fixed-point for a=4.
     * 0x00000005 is a fixed-point for a=5.
     * 0x00000006 is a fixed-point for a=6.
     * 0x05DC4BC1 is a fixed-point for a=6.
     * 0x29A00EC9 is a fixed-point for a=6.
     * 0x37B8EFAD is a fixed-point for a=6.
     * 0x00000007 is a fixed-point for a=7.
     * 0x00000008 is a fixed-point for a=8.
     * 0x54F4B63F is a fixed-point for a=8.
     * 0x61C43937 is a fixed-point for a=8.
     * 0xA26E804E is a fixed-point for a=8.
     * 0x00000009 is a fixed-point for a=9.
     * 0xA26E804F is a fixed-point for a=9.
     * 0xDAC3335C is a fixed-point for a=9.
     * 0x0000000A is a fixed-point for a=10.
     * 0x44394305 is a fixed-point for a=10.
     * 0x0000000B is a fixed-point for a=11.
     * 0xDAC3335E is a fixed-point for a=11.
     * 0x0000000C is a fixed-point for a=12.
     * 0x0000000D is a fixed-point for a=13.
     * 0x8A06FD30 is a fixed-point for a=13.
     * 0x0000000E is a fixed-point for a=14.
     * 0x05DC4BC9 is a fixed-point for a=14.
     * 0x0000000F is a fixed-point for a=15.
     * 0x8A06FD32 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainXEA(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx ^ a) + a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * Not-encrypt-add doesn't do much better, though the fixed-points are unpredictable.
     * <br>
     * 0x773D1C2F is a fixed-point for a=1.
     * 0xC9E08ACF is a fixed-point for a=1.
     * 0xD37BECDB is a fixed-point for a=2.
     * 0x0566B1F9 is a fixed-point for a=3.
     * 0x2B0288C6 is a fixed-point for a=3.
     * 0xF6484DBF is a fixed-point for a=3.
     * 0x2111917A is a fixed-point for a=7.
     * 0x5B873127 is a fixed-point for a=11.
     * 0xECE9D0E1 is a fixed-point for a=11.
     * 0x61DEEB54 is a fixed-point for a=12.
     * 0xDEB0AD17 is a fixed-point for a=12.
     * 0x354E1792 is a fixed-point for a=13.
     * 0x632B4FD2 is a fixed-point for a=13.
     * 0x0A07FE85 is a fixed-point for a=14.
     * 0x1095EFFD is a fixed-point for a=14.
     * 0x852B346F is a fixed-point for a=14.
     * 0xD1B5D799 is a fixed-point for a=14.
     * 0xDECA3C51 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainNEA(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(~idx) + a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * NOT-encrypt-XOR.
     * <br>
     * 0x773D1C2F is a fixed-point for a=1.
     * 0x796D5548 is a fixed-point for a=1.
     * 0xC9E08ACF is a fixed-point for a=1.
     * 0xD37BECDB is a fixed-point for a=2.
     * 0xEF301BF0 is a fixed-point for a=2.
     * 0xF6484DBF is a fixed-point for a=3.
     * 0x2B0288C6 is a fixed-point for a=5.
     * 0x7C6962A2 is a fixed-point for a=5.
     * 0xAC82C534 is a fixed-point for a=8.
     * 0x2111917A is a fixed-point for a=9.
     * 0x34F36FA1 is a fixed-point for a=10.
     * 0x2A9A3447 is a fixed-point for a=11.
     * 0x852B346F is a fixed-point for a=14.
     * 0x0566B1F9 is a fixed-point for a=15.
     * 0x66940A60 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainNEX(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(~idx) ^ a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * NOT-Encrypt-Minus.
     * <br>
     * 0x796D5548 is a fixed-point for a=1.
     * 0xFFFFFFFF is a fixed-point for a=1.
     * 0xEF301BF0 is a fixed-point for a=2.
     * 0x757F9AED is a fixed-point for a=4.
     * 0x2A9A3447 is a fixed-point for a=5.
     * 0x7C6962A2 is a fixed-point for a=5.
     * 0xA594226D is a fixed-point for a=8.
     * 0xAC82C534 is a fixed-point for a=8.
     * 0x34F36FA1 is a fixed-point for a=10.
     * 0xFDFC850F is a fixed-point for a=11.
     * 0x78C5788C is a fixed-point for a=14.
     * 0x9B1DE486 is a fixed-point for a=14.
     * 0x66940A60 is a fixed-point for a=15.
     * 0x823A2A74 is a fixed-point for a=15.
     *
     * @param args
     */
    public static void mainNEM(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(~idx) - a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * XOR with {@code a} before passing to scrambleInt().
     * <br>
     * 0x1B32410C is a fixed-point for a=-1.
     * 0x7F419D57 is a fixed-point for a=-1.
     * 0x00000000 is a fixed-point for a=0.
     * 0x54F4B637 is a fixed-point for a=0.
     * 0xA26E8046 is a fixed-point for a=0.
     * 0x17FD53DC is a fixed-point for a=2.
     * 0x9384DF05 is a fixed-point for a=4.
     * 0xE4F04311 is a fixed-point for a=4.
     * 0xD43A4C34 is a fixed-point for a=6.
     * 0xDAC33353 is a fixed-point for a=6.
     * 0x4428DE41 is a fixed-point for a=7.
     * 0x8329E33B is a fixed-point for a=7.
     * 0x8B3C89E3 is a fixed-point for a=10.
     * 0x681470DC is a fixed-point for a=11.
     * 0x29A00EC3 is a fixed-point for a=12.
     * 0x37B8EFA7 is a fixed-point for a=12.
     * 0x976BF754 is a fixed-point for a=12.
     *
     * @param args
     */
    public static void mainPreXor(String[] args){
        int idx = 0;
        for (int a = -1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx ^ a)))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * XOR with {@code a} after passing to scrambleInt().
     * <br>
     * 0x80BE62A8 is a fixed-point for a=-1.
     * 0xE4CDBEF3 is a fixed-point for a=-1.
     * 0x00000000 is a fixed-point for a=0.
     * 0x54F4B637 is a fixed-point for a=0.
     * 0xA26E8046 is a fixed-point for a=0.
     * 0x17FD53DE is a fixed-point for a=2.
     * 0x9384DF01 is a fixed-point for a=4.
     * 0xE4F04315 is a fixed-point for a=4.
     * 0xD43A4C32 is a fixed-point for a=6.
     * 0xDAC33355 is a fixed-point for a=6.
     * 0x4428DE46 is a fixed-point for a=7.
     * 0x8329E33C is a fixed-point for a=7.
     * 0x8B3C89E9 is a fixed-point for a=10.
     * 0x681470D7 is a fixed-point for a=11.
     * 0x29A00ECF is a fixed-point for a=12.
     * 0x37B8EFAB is a fixed-point for a=12.
     * 0x976BF758 is a fixed-point for a=12.
     * <br>
     * The fixed-points for pre- and post-XOR are just one XOR with {@code a} apart.
     *
     * @param args
     */
    public static void mainPostXor(String[] args){
        int idx = 0;
        for (int a = -1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx) ^ a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * In 0.3.1: 0xBD0F4FA9 and 0xE7AFC875 are fixed-points for scrambleInt().
     */
    public static void mainInt(String[] args){
        int idx = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 0x10000000; j++) {
                if(idx == Scramblers.scrambleInt(idx))
                    System.err.printf(" 0x%08X is a fixed-point.\n", idx);
                idx++;
            }
            System.out.println("Completed " + (i+1) + "/16");
        }
    }
}
