package com.nfcat.nktool;

import java.text.SimpleDateFormat;
import java.util.*;

public class Study {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.next();
        String[] split = s.split("");
        if (s.length() == 1 || (split.length == 2 && split[0] == split[1])) {
            System.out.println(s);
            return;
        }
        String[] ls;
        if (split.length % 2 == 0) {
            ls = Arrays.copyOf(split, split.length + 1);
            ls[ls.length - 1] = split[0];
        } else {
            ls = split;
        }
        System.out.println(Arrays.toString(ls));
        int x = 0;
        int m = 0;
        for (int i = split.length - 1; i > 1; i--) {
            x++;
            if (!ls[i - x].equals(ls[i + x])) continue;
            System.out.println(ls[i - x]);
            System.out.println(ls[i + x]);
            m = i + 1;
        }
        if (m != 0) {
            System.out.println(m);
        } else {
            System.out.println(s);
        }
    }

    public static void main4(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        for (int i = 0; i < n; i++) {
            String s = sc.next();
            if (!list.contains(s)) {
                list.add(s);
            }
        }
        for (String s : list) {
            System.out.println(s);
        }
    }

    public static void main3(String[] args) {
        Scanner sc = new Scanner(System.in);
        double t = sc.nextDouble();
        double c = sc.nextDouble();
        double s = sc.nextDouble();
        System.out.println(Integer.parseInt(("" + (s - c) / (c / t)).replace(".0", "")));
    }

    public static void mainX1(String[] args) {
        String x;
        for (int i = 10; i < 99999; i++) {
            x = "" + i;
            int n = 0;
            HexFormat.fromHexDigits(x);
            for (int j = 0; j < x.length(); j++) {
                int i1 = Integer.parseInt(String.valueOf(x.charAt(j)));
                n += i1 * Math.pow(16, j);
            }
            if (n % i == 0) {
                System.out.println(n);
                System.out.println(i);
                break;
            }
            i++;
        }
    }

    public static void main2(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        long time = format.parse("1949-10-1").getTime();
        long time1 = format.parse("2022-1-1").getTime();
        System.out.println((time1 - time) / 1000 / 60 / 60 / 24);
    }

    public static void main1(String[] args) {
        int i = 2022;
        String s;
        while (true) {
            s = Integer.toBinaryString(i);
            if (s.endsWith("000000")) {
                System.out.println(i);
                System.out.println(s);
                return;
            }
            i++;
        }
    }
}
