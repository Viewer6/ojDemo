-- 15 道 Java 判题题目
-- 使用约定：
--   1. default_code 是完整的 Solution 类，main_fuc 是完整的 Main 类，
--      后端会分别写入 Solution.java 和 Main.java 后一起编译。
--   2. time_limit 单位是毫秒，space_limit 单位是字节（Docker 返回的内存也是字节）。
--   3. question_case 是 JSON 数组，input 中的数字用空格分隔，Main 用 Scanner 读取。
--   4. question_id 使用 101-115，如果已被占用请自行调整。

SET NAMES utf8mb4;

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(101, '回文数', 1, 2000, 268435456,
 '# 回文数

## 题目描述
给你一个整数 x，如果 x 是一个回文整数，返回 true；否则返回 false。
回文数是指正序和倒序读都一样的整数。例如 121 是回文数，-121 不是。

## 输入描述
输入一个整数 x。

## 输出描述
输出 true 或 false。

## 示例 1
- 输入：`121`
- 输出：`true`

## 示例 2
- 输入：`-121`
- 输出：`false`

## 示例 3
- 输入：`10`
- 输出：`false`',
 '[{"input":"121","output":"true"},{"input":"-121","output":"false"},{"input":"10","output":"false"},{"input":"0","output":"true"}]',
 'import java.util.*;

public class Solution {
    public boolean isPalindrome(int x) {
        // 在这里写你的实现
        return false;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int x = sc.nextInt();
        sc.close();
        boolean ans = new Solution().isPalindrome(x);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(102, '罗马数字转整数', 1, 2000, 268435456,
 '# 罗马数字转整数

## 题目描述
罗马数字包含七种字符：I、V、X、L、C、D、M，分别表示 1、5、10、50、100、500、1000。
通常情况下，小的数字在大的数字右边；若小的数字在大的数字左边，则表示大数减小数，例如 IV 表示 4，IX 表示 9。
给定一个罗马数字，将其转换成整数。

## 输入描述
输入一个只包含罗马数字字符的字符串 s。

## 输出描述
输出转换后的整数。

## 示例 1
- 输入：`III`
- 输出：`3`

## 示例 2
- 输入：`IV`
- 输出：`4`

## 示例 3
- 输入：`MCMXCIV`
- 输出：`1994`',
 '[{"input":"III","output":"3"},{"input":"IV","output":"4"},{"input":"IX","output":"9"},{"input":"LVIII","output":"58"},{"input":"MCMXCIV","output":"1994"}]',
 'import java.util.*;

public class Solution {
    public int romanToInt(String s) {
        // 在这里写你的实现
        return 0;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.next();
        sc.close();
        int ans = new Solution().romanToInt(s);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(103, '最长公共前缀', 1, 2000, 268435456,
 '# 最长公共前缀

## 题目描述
编写一个函数来查找字符串数组中的最长公共前缀。
如果不存在公共前缀，返回空字符串。

## 输入描述
输入整数 n，表示字符串个数，随后输入 n 个只包含小写字母的字符串，用空格分隔。

## 输出描述
输出最长公共前缀；不存在时输出空字符串。

## 示例 1
- 输入：`3 flower flow flight`
- 输出：`fl`

## 示例 2
- 输入：`3 dog racecar car`
- 输出：空字符串

## 示例 3
- 输入：`1 a`
- 输出：`a`',
 '[{"input":"3 flower flow flight","output":"fl"},{"input":"3 dog racecar car","output":""},{"input":"1 a","output":"a"},{"input":"2 ab ab","output":"ab"}]',
 'import java.util.*;

public class Solution {
    public String longestCommonPrefix(String[] strs) {
        // 在这里写你的实现
        return "";
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        String[] strs = new String[n];
        for (int i = 0; i < n; i++) {
            strs[i] = sc.next();
        }
        sc.close();
        String ans = new Solution().longestCommonPrefix(strs);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(104, '有效的括号', 1, 2000, 268435456,
 '# 有效的括号

## 题目描述
给定一个只包含三种括号的字符串 s，判断字符串是否有效。
有效字符串需满足：左括号必须用相同类型的右括号闭合，且左括号必须以正确的顺序闭合。

## 输入描述
输入一个只包含括号字符的字符串 s。

## 输出描述
输出 true 或 false。

## 示例 1
- 输入：`()`
- 输出：`true`

## 示例 2
- 输入：`([)]`
- 输出：`false`

## 示例 3
- 输入：`{[]}`
- 输出：`true`',
 '[{"input":"()","output":"true"},{"input":"()[]{}","output":"true"},{"input":"(]","output":"false"},{"input":"([)]","output":"false"},{"input":"{[]}","output":"true"}]',
 'import java.util.*;

public class Solution {
    public boolean isValid(String s) {
        // 在这里写你的实现
        return false;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.next();
        sc.close();
        boolean ans = new Solution().isValid(s);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(105, '爬楼梯', 1, 2000, 268435456,
 '# 爬楼梯

## 题目描述
假设你正在爬楼梯，需要 n 阶才能到达楼顶。
每次你可以爬 1 或 2 个台阶，问有多少种不同的方法可以爬到楼顶。

## 输入描述
输入一个正整数 n。

## 输出描述
输出爬到楼顶的方案数。

## 示例 1
- 输入：`2`
- 输出：`2`

## 示例 2
- 输入：`3`
- 输出：`3`

## 示例 3
- 输入：`5`
- 输出：`8`',
 '[{"input":"1","output":"1"},{"input":"2","output":"2"},{"input":"3","output":"3"},{"input":"5","output":"8"}]',
 'import java.util.*;

public class Solution {
    public int climbStairs(int n) {
        // 在这里写你的实现
        return 0;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        sc.close();
        int ans = new Solution().climbStairs(n);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(106, '只出现一次的数字', 1, 2000, 268435456,
 '# 只出现一次的数字

## 题目描述
给定一个非空整数数组，除了某个元素只出现一次以外，其余每个元素均出现两次。
找出那个只出现一次的元素。要求线性时间复杂度，并且只使用常量额外空间。

## 输入描述
输入整数 n，表示数组长度，随后输入 n 个整数，用空格分隔。

## 输出描述
输出只出现一次的元素。

## 示例 1
- 输入：`3 2 2 1`
- 输出：`1`

## 示例 2
- 输入：`5 4 1 2 1 2`
- 输出：`4`

## 示例 3
- 输入：`1 1`
- 输出：`1`',
 '[{"input":"3 2 2 1","output":"1"},{"input":"5 4 1 2 1 2","output":"4"},{"input":"1 1","output":"1"},{"input":"7 1 1 2 2 3 3 4","output":"4"}]',
 'import java.util.*;

public class Solution {
    public int singleNumber(int[] nums) {
        // 在这里写你的实现
        return 0;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }
        sc.close();
        int ans = new Solution().singleNumber(nums);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(107, '多数元素', 1, 2000, 268435456,
 '# 多数元素

## 题目描述
给定一个大小为 n 的数组，找到其中的多数元素。
多数元素是指在数组中出现次数大于 n/2 的元素。你可以假设数组非空，并且多数元素总是存在。

## 输入描述
输入整数 n，表示数组长度，随后输入 n 个整数，用空格分隔。

## 输出描述
输出多数元素。

## 示例 1
- 输入：`3 3 2 3`
- 输出：`3`

## 示例 2
- 输入：`7 2 2 1 1 1 2 2`
- 输出：`2`

## 示例 3
- 输入：`1 5`
- 输出：`5`',
 '[{"input":"3 3 2 3","output":"3"},{"input":"7 2 2 1 1 1 2 2","output":"2"},{"input":"1 5","output":"5"}]',
 'import java.util.*;

public class Solution {
    public int majorityElement(int[] nums) {
        // 在这里写你的实现
        return 0;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }
        sc.close();
        int ans = new Solution().majorityElement(nums);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(108, '移动零', 1, 2000, 268435456,
 '# 移动零

## 题目描述
给定一个数组 nums，编写一个函数将所有 0 移动到数组的末尾，同时保持非零元素的相对顺序。
必须在原数组上操作，不能拷贝额外的数组。

## 输入描述
输入整数 n，表示数组长度，随后输入 n 个整数，用空格分隔。

## 输出描述
输出移动后的数组，元素之间用空格分隔。

## 示例 1
- 输入：`5 0 1 0 3 12`
- 输出：`1 3 12 0 0`

## 示例 2
- 输入：`1 0`
- 输出：`0`

## 示例 3
- 输入：`2 1 2`
- 输出：`1 2`',
 '[{"input":"5 0 1 0 3 12","output":"1 3 12 0 0"},{"input":"1 0","output":"0"},{"input":"2 1 2","output":"1 2"},{"input":"6 0 0 1 0 2 0","output":"1 2 0 0 0 0"}]',
 'import java.util.*;

public class Solution {
    public int[] moveZeroes(int[] nums) {
        // 在这里写你的实现
        return nums;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }
        sc.close();
        int[] ans = new Solution().moveZeroes(nums);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ans.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(ans[i]);
        }
        System.out.println(sb.toString());
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(109, '二分查找', 1, 2000, 268435456,
 '# 二分查找

## 题目描述
给定一个升序排列且元素互不相同的整数数组 nums 和一个目标值 target，在数组中搜索 target。
如果存在，返回其下标；否则返回 -1。要求时间复杂度为 O(log n)。

## 输入描述
输入整数 n，随后输入 n 个升序排列的整数，最后输入目标值 target，均用空格分隔。

## 输出描述
输出 target 的下标；不存在时输出 -1。

## 示例 1
- 输入：`6 -1 0 3 5 9 12 9`
- 输出：`4`

## 示例 2
- 输入：`6 -1 0 3 5 9 12 2`
- 输出：`-1`

## 示例 3
- 输入：`1 5 5`
- 输出：`0`',
 '[{"input":"6 -1 0 3 5 9 12 9","output":"4"},{"input":"6 -1 0 3 5 9 12 2","output":"-1"},{"input":"1 5 5","output":"0"}]',
 'import java.util.*;

public class Solution {
    public int search(int[] nums, int target) {
        // 在这里写你的实现
        return -1;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }
        int target = sc.nextInt();
        sc.close();
        int ans = new Solution().search(nums, target);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(110, '加一', 1, 2000, 268435456,
 '# 加一

## 题目描述
给定一个由整数组成的非空数组所表示的非负整数，在该数的基础上加一。
最高位数字存放在数组首位，数组中每个元素只存储单个数字。你可以假设除了整数 0 之外，这个整数不会以零开头。

## 输入描述
输入整数 n，随后输入 n 个数字，用空格分隔。

## 输出描述
输出加一后的数组，元素之间用空格分隔。

## 示例 1
- 输入：`3 1 2 3`
- 输出：`1 2 4`

## 示例 2
- 输入：`1 9`
- 输出：`1 0`

## 示例 3
- 输入：`2 9 9`
- 输出：`1 0 0`',
 '[{"input":"3 1 2 3","output":"1 2 4"},{"input":"4 4 3 2 1","output":"4 3 2 2"},{"input":"1 9","output":"1 0"},{"input":"2 9 9","output":"1 0 0"}]',
 'import java.util.*;

public class Solution {
    public int[] plusOne(int[] digits) {
        // 在这里写你的实现
        return digits;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] digits = new int[n];
        for (int i = 0; i < n; i++) {
            digits[i] = sc.nextInt();
        }
        sc.close();
        int[] ans = new Solution().plusOne(digits);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ans.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(ans[i]);
        }
        System.out.println(sb.toString());
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(111, '无重复字符的最长子串', 2, 3000, 268435456,
 '# 无重复字符的最长子串

## 题目描述
给定一个字符串 s，请你找出其中不含有重复字符的最长子串的长度。
注意子串是连续的字符序列。

## 输入描述
输入一个字符串 s，字符串中不包含空格。

## 输出描述
输出最长子串的长度。

## 示例 1
- 输入：`abcabcbb`
- 输出：`3`

## 示例 2
- 输入：`bbbbb`
- 输出：`1`

## 示例 3
- 输入：`pwwkew`
- 输出：`3`',
 '[{"input":"abcabcbb","output":"3"},{"input":"bbbbb","output":"1"},{"input":"pwwkew","output":"3"},{"input":"au","output":"2"},{"input":"dvdf","output":"3"}]',
 'import java.util.*;

public class Solution {
    public int lengthOfLongestSubstring(String s) {
        // 在这里写你的实现
        return 0;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.next();
        sc.close();
        int ans = new Solution().lengthOfLongestSubstring(s);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(112, '最大子数组和', 2, 3000, 268435456,
 '# 最大子数组和

## 题目描述
给你一个整数数组 nums，请你找出一个具有最大和的连续子数组（子数组最少包含一个元素），返回其最大和。

## 输入描述
输入整数 n，随后输入 n 个整数，用空格分隔。

## 输出描述
输出最大子数组和。

## 示例 1
- 输入：`9 -2 1 -3 4 -1 2 1 -5 4`
- 输出：`6`

## 示例 2
- 输入：`1 1`
- 输出：`1`

## 示例 3
- 输入：`5 -2 -3 -1 -5 -4`
- 输出：`-1`',
 '[{"input":"9 -2 1 -3 4 -1 2 1 -5 4","output":"6"},{"input":"1 1","output":"1"},{"input":"5 5 4 -1 7 8","output":"23"},{"input":"5 -2 -3 -1 -5 -4","output":"-1"}]',
 'import java.util.*;

public class Solution {
    public int maxSubArray(int[] nums) {
        // 在这里写你的实现
        return 0;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }
        sc.close();
        int ans = new Solution().maxSubArray(nums);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(113, '买卖股票的最佳时机', 2, 3000, 268435456,
 '# 买卖股票的最佳时机

## 题目描述
给定一个数组 prices，它的第 i 个元素 prices[i] 表示一支给定股票第 i 天的价格。
你只能选择某一天买入这只股票，并选择在未来的某一个不同的日子卖出。返回你能获取的最大利润；如果无法获取利润，返回 0。

## 输入描述
输入整数 n，随后输入 n 个整数，表示每天的价格，用空格分隔。

## 输出描述
输出最大利润。

## 示例 1
- 输入：`6 7 1 5 3 6 4`
- 输出：`5`

## 示例 2
- 输入：`5 7 6 4 3 1`
- 输出：`0`

## 示例 3
- 输入：`3 1 2 4`
- 输出：`3`',
 '[{"input":"6 7 1 5 3 6 4","output":"5"},{"input":"5 7 6 4 3 1","output":"0"},{"input":"2 2 4","output":"2"},{"input":"3 1 2 4","output":"3"}]',
 'import java.util.*;

public class Solution {
    public int maxProfit(int[] prices) {
        // 在这里写你的实现
        return 0;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] prices = new int[n];
        for (int i = 0; i < n; i++) {
            prices[i] = sc.nextInt();
        }
        sc.close();
        int ans = new Solution().maxProfit(prices);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(114, '零钱兑换', 2, 3000, 268435456,
 '# 零钱兑换

## 题目描述
给你一个整数数组 coins，表示不同面额的硬币，以及一个整数 amount 表示总金额。
计算并返回可以凑成总金额所需的最少硬币个数。如果没有任何一种硬币组合能组成总金额，返回 -1。
每种硬币的数量是无限的。

## 输入描述
输入整数 n，随后输入 n 个硬币面额，最后输入总金额 amount，用空格分隔。

## 输出描述
输出最少硬币个数；无法凑成时输出 -1。

## 示例 1
- 输入：`3 1 2 5 11`
- 输出：`3`

## 示例 2
- 输入：`1 2 3`
- 输出：`-1`

## 示例 3
- 输入：`1 1 0`
- 输出：`0`',
 '[{"input":"3 1 2 5 11","output":"3"},{"input":"1 2 3","output":"-1"},{"input":"1 1 0","output":"0"},{"input":"2 2 5 3","output":"-1"}]',
 'import java.util.*;

public class Solution {
    public int coinChange(int[] coins, int amount) {
        // 在这里写你的实现
        return -1;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] coins = new int[n];
        for (int i = 0; i < n; i++) {
            coins[i] = sc.nextInt();
        }
        int amount = sc.nextInt();
        sc.close();
        int ans = new Solution().coinChange(coins, amount);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);

INSERT INTO tb_question
(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time, update_by, update_time)
VALUES
(115, '接雨水', 3, 3000, 268435456,
 '# 接雨水

## 题目描述
给定 n 个非负整数表示每个宽度为 1 的柱子的高度图，计算按此排列的柱子，下雨之后能接多少雨水。

## 输入描述
输入整数 n，随后输入 n 个非负整数，表示柱子的高度，用空格分隔。

## 输出描述
输出能接的雨水总量。

## 示例 1
- 输入：`12 0 1 0 2 1 0 1 3 2 1 2 1`
- 输出：`6`

## 示例 2
- 输入：`6 4 2 0 3 2 5`
- 输出：`9`

## 示例 3
- 输入：`3 1 0 2`
- 输出：`1`',
 '[{"input":"12 0 1 0 2 1 0 1 3 2 1 2 1","output":"6"},{"input":"6 4 2 0 3 2 5","output":"9"},{"input":"3 1 0 2","output":"1"}]',
 'import java.util.*;

public class Solution {
    public int trap(int[] height) {
        // 在这里写你的实现
        return 0;
    }
}',
 'import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] height = new int[n];
        for (int i = 0; i < n; i++) {
            height[i] = sc.nextInt();
        }
        sc.close();
        int ans = new Solution().trap(height);
        System.out.println(ans);
    }
}',
 1001, NOW(), NULL, NULL);
