package com.insurance.claimapi.util;

public class TopFiveSimple {
    public static int[] getTopFive(int[] nums) {
        // 1. 先取前5个
        int[] result = new int[5];
        for (int i = 0; i < 5; i++) {
            result[i] = nums[i];
        }
        
        // 2. 遍历剩余元素
        for (int i = 5; i < nums.length; i++) {
            // 3. 找到当前5个中最小的
            int minIndex = 0;
            for (int j = 1; j < 5; j++) {
                if (result[j] < result[minIndex]) {
                    minIndex = j;
                }
            }
            // 4. 如果当前数比最小的大，替换
            if (nums[i] > result[minIndex]) {
                result[minIndex] = nums[i];
            }
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        int[] nums = {23, 45, 12, 67, 89, 34, 56, 78, 91, 5};
        int[] topFive = getTopFive(nums);
        
        System.out.print("原数组: ");
        for (int num : nums) {
            System.out.print(num + " ");
        }
        System.out.println();
        
        System.out.print("最大的5个数: ");
        for (int num : topFive) {
            System.out.print(num + " ");
        }
        // 输出：最大的5个数: 91 89 78 67 56
    }
}