# 创建管理员表
create table tb_sys_user (
                             user_id      bigint unsigned not null comment '用户id（主键）',
                             user_account varchar(20) not null  comment '账号',
                             nick_name    varchar(20) comment '昵称',
                             password     char(60) not null  comment '密码',
                             create_by    bigint unsigned not null  comment '创建人',
                             create_time  datetime not null comment '创建时间',
                             update_by    bigint unsigned  comment '更新人',
                             update_time  datetime comment '跟新时间',
                             primary key (`user_id`),
                             unique key `idx_user_account` (`user_account`)
);

# 这里不使用自增id的原因: 方便数据迁移和备份, 删除和插入操作不会浪费空间, 解决性能问题, 可预测性强, 解决分布式环境自增id唯一的问题

# --题库管理
#
# B端：列表功能、添加题目、编辑、删除
#
#
# C端：题库列表功能、题目热榜列表、答题、竞赛开始答题、竞赛练习
# 题目数据      ----》数据库  mysql    设计表结构
# 满足需求  避免冗余设计  考虑今后发展
# 题目内容： 长度变化
# 两数之和  1 + 2 = 3
#
# [
#     {
#         "input": "1 2",
#         "output": "3"
#     },
#     {
#         "input": "4 5",
#         "output": "9"
#     }
# ]
# 时间限制 ：毫秒    空间限制：字节
# delete  from  tb_question where question_id =
create table tb_question(
                            question_id bigint unsigned not null comment '题目id',
                            title varchar(50) not null  comment '题目标题',
                            difficulty tinyint not null comment '题目难度1:简单  2：中等 3：困难',
                            time_limit int not null comment '时间限制',
                            space_limit int not null comment '空间限制',
                            content varchar(1000) not null comment '题目内容',
                            question_case varchar(1000)  comment '题目用例',
                            default_code varchar(500) not null comment '默认代码块',
                            main_fuc varchar(500) not null comment 'main函数',
                            create_by    bigint unsigned not null  comment '创建人',
                            create_time  datetime not null comment '创建时间',
                            update_by    bigint unsigned  comment '更新人',
                            update_time  datetime comment '更新时间',
                            primary key(`question_id`)
);

INSERT INTO tb_question (
    question_id, title, difficulty, time_limit, space_limit, content, question_case,
    default_code, main_fuc, create_by, create_time, update_by, update_time
) VALUES
      (1, '两数之和', 1, 1, 256, '给定一个整数数组和一个目标值，找出和为目标值的两个数。', '[2,7,11,15],9 -> [0,1]', 'def twoSum(nums, target):', 'print(twoSum([2,7,11,15], 9))', 1001, NOW(), NULL, NULL),
      (2, '反转字符串', 1, 1, 128, '编写一个函数，反转输入的字符串。', '"hello" -> "olleh"', 'def reverseString(s):', 'print(reverseString("hello"))', 1002, NOW(), NULL, NULL),
      (3, '斐波那契数列', 1, 1, 128, '求第 n 个斐波那契数。', 'n=5 -> 5', 'def fib(n):', 'print(fib(5))', 1003, NOW(), NULL, NULL),
      (4, '有效的括号', 2, 2, 256, '判断括号字符串是否合法。', '"()[]{}" -> True', 'def isValid(s):', 'print(isValid("()[]{}"))', 1001, NOW(), NULL, NULL),
      (5, '合并两个有序链表', 2, 2, 512, '将两个升序链表合并为一个新的升序链表。', '[1,2,4],[1,3,4] -> [1,1,2,3,4,4]', 'def mergeTwoLists(l1, l2):', 'print(mergeTwoLists([1,2,4], [1,3,4]))', 1002, NOW(), NULL, NULL),
      (6, '最大子数组和', 2, 2, 256, '找出具有最大和的连续子数组。', '[-2,1,-3,4,-1,2,1,-5,4] -> 6', 'def maxSubArray(nums):', 'print(maxSubArray([-2,1,-3,4,-1,2,1,-5,4]))', 1003, NOW(), NULL, NULL),
      (7, '罗马数字转整数', 1, 1, 128, '将罗马数字转换成整数。', '"III" -> 3', 'def romanToInt(s):', 'print(romanToInt("III"))', 1001, NOW(), NULL, NULL),
      (8, '最长公共前缀', 2, 2, 256, '查找字符串数组中的最长公共前缀。', '["flower","flow","flight"] -> "fl"', 'def longestCommonPrefix(strs):', 'print(longestCommonPrefix(["flower","flow","flight"]))', 1002, NOW(), NULL, NULL),
      (9, '搜索插入位置', 1, 1, 128, '给定排序数组和目标值，返回插入位置。', '[1,3,5,6],5 -> 2', 'def searchInsert(nums, target):', 'print(searchInsert([1,3,5,6],5))', 1003, NOW(), NULL, NULL),
      (10, '爬楼梯', 2, 2, 256, '每次可以爬 1 或 2 阶台阶，计算爬到顶部的方式总数。', 'n=3 -> 3', 'def climbStairs(n):', 'print(climbStairs(3))', 1001, NOW(), NULL, NULL),
      (11, '验证回文串', 1, 1, 128, '判断是否为回文字符串，忽略非字母数字字符和大小写。', '"A man, a plan, a canal: Panama" -> True', 'def isPalindrome(s):', 'print(isPalindrome("A man, a plan, a canal: Panama"))', 1002, NOW(), NULL, NULL),
      (12, '二分查找', 1, 1, 128, '实现一个二分查找算法。', '[1,2,3,4,5],3 -> 2', 'def binarySearch(nums, target):', 'print(binarySearch([1,2,3,4,5],3))', 1003, NOW(), NULL, NULL);

