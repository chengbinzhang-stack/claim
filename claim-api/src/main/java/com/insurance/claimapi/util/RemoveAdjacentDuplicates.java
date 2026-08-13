package com.insurance.claimapi.util;
public class RemoveAdjacentDuplicates {
    public static String removeAdjacentDuplicates(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        StringBuilder result = new StringBuilder();
        result.append(str.charAt(0)); // 先加入第一个字符
        
        for (int i = 1; i < str.length(); i++) {
            // 如果当前字符与前一个字符不同，则加入
            if (str.charAt(i) != str.charAt(i - 1)) {
                result.append(str.charAt(i));
            }
        }
        
        return result.toString();
    }
    
    public static void main(String[] args) {
        String input = "abbcccdbe";
        String output = removeAdjacentDuplicates(input);
        System.out.println("输入: " + input);
        System.out.println("输出: " + output);
        // 输出: 输入: abbcccdbe
        //       输出: abcdbe
        
        // 更多测试用例
        System.out.println("aaabbbccc -> " + removeAdjacentDuplicates("aaabbbccc")); // abc
        System.out.println("aabbcc -> " + removeAdjacentDuplicates("aabbcc")); // abc
        System.out.println("abcde -> " + removeAdjacentDuplicates("abcde")); // abcde
        System.out.println("a -> " + removeAdjacentDuplicates("a")); // a
    }
}