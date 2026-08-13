package com.insurance.claimapi.util;

public class MaxProfit {
    public int maxProfit(int[] prices) {
        int buyPrice = prices[0];
        int maxProfit = 0;
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] < buyPrice) {
                buyPrice = prices[i];
            } else {
                int profit = prices[i] - buyPrice;
                if (maxProfit < profit) {
                    maxProfit = profit;
                }
            }            

        }
        return maxProfit;
        
    }

    public static void main(String[] args) {
        MaxProfit maxProfit = new MaxProfit();
        int[] prices = {7, 1, 5, 3, 6, 4};
        int profit = maxProfit.maxProfit(prices);
        System.out.println("最大利润: " + profit); // 输出：最大利润: 5
    }

}
