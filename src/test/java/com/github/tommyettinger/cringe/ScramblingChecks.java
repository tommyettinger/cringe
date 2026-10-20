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
    public static void main(String[] args){
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
     *
     * @param args
     */
    public static void mainAEMD(String[] args){
        int idx = 0;
        for (int a = -16; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx + a) - a - 1))
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
