
-- 题目初始数据
INSERT INTO tb_question(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time)
VALUES (1, '两数之和', 2, 1000, 256, '给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那两个整数，并返回他们的数组下标。', '[1, 3, 4, 2], 6', 'import java.util.HashMap;\n\npublic class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        HashMap<Integer, Integer> map = new HashMap<>();\n        for (int i = 0; i < nums.length; i++) {\n            int complement = target - nums[i];\n            if (map.containsKey(complement)) {\n                return new int[] { map.get(complement), i };\n            }\n            map.put(nums[i], i);\n        }\n        throw new IllegalArgumentException("No two sum solution");\n    }\n}', 'public static void main(String[] args) {\n    Solution solution = new Solution();\n    int[] result = solution.twoSum(new int[]{1, 3, 4, 2}, 6);\n    System.out.println(result[0] + ", " + result[1]);\n}', 1, '2025-01-01 10:00:00');

INSERT INTO tb_question(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time)
VALUES (2, '反转字符串', 1, 500, 128, '编写一个函数，其作用是将输入的字符串反转过来。输入字符串以字符数组 char[] 的形式给出。', '"hello"', 'public class Solution {\n    public void reverseString(char[] s) {\n        int n = s.length;\n        for (int i = 0; i < n / 2; i++) {\n            char temp = s[i];\n            s[i] = s[n - 1 - i];\n            s[n - 1 - i] = temp;\n        }\n    }\n}', 'public static void main(String[] args) {\n    Solution solution = new Solution();\n    char[] s = {"h", "e", "l", "l", "o"};\n    solution.reverseString(s);\n    System.out.println(java.util.Arrays.toString(s));\n}', 1, '2025-01-02 11:00:00');

INSERT INTO tb_question(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time)
VALUES (3, '最长公共前缀', 2, 1500, 256, '编写一个函数来查找字符串数组中的最长公共前缀。如果不存在公共前缀，返回空字符串 ""。', '["flower","flow","flight"]', 'public class Solution {\n    public String longestCommonPrefix(String[] strs) {\n        if (strs == null || strs.length == 0) return "";\n        String prefix = strs[0];\n        for (int i = 1; i < strs.length; i++)\n            while (strs[i].indexOf(prefix) != 0) {\n                prefix = prefix.substring(0, prefix.length() - 1);\n                if (prefix.isEmpty()) return "";\n            }\n        return prefix;\n    }\n}', 'public static void main(String[] args) {\n    Solution solution = new Solution();\n    String commonPrefix = solution.longestCommonPrefix(new String[]{"flower","flow","flight"});\n    System.out.println(commonPrefix);\n}', 1, '2025-01-03 12:00:00');

INSERT INTO tb_question(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time)
VALUES (4, '有效的括号', 2, 1000, 128, '给定一个只包括 "("，")"，"{"，"}"，"["，"]" 的字符串，判断字符串是否有效。', '"()[]{}"', 'import java.util.Stack;\npublic class Solution {\n    public boolean isValid(String s) {\n        // 在这里编写你的代码\n    }\n}', 'public static void main(String[] args) {\n    Solution solution = new Solution();\n    System.out.println(solution.isValid("()[]{}"));\n}', 1, '2025-01-04 13:00:00');

INSERT INTO tb_question(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time)
VALUES (5, '合并两个有序链表', 2, 2000, 256, '将两个升序链表合并为一个新的升序链表并返回。新链表是通过拼接给定的两个链表的所有节点组成的。', '[1->2->4, 1->3->4]', 'class ListNode {\n    int val;\n    ListNode next;\n    ListNode(int x) { val = x; }\n}\n\npublic class Solution {\n    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {\n        // 在这里编写你的代码\n    }\n}', 'public static void main(String[] args) {\n    // 假设已构建好链表l1和l2\n    ListNode l1 = new ListNode(1);\n    l1.next = new ListNode(2); // 示例链表构造\n    ListNode l2 = new ListNode(1);\n    Solution solution = new Solution();\n    ListNode mergedList = solution.mergeTwoLists(l1, l2);\n}', 1, '2025-01-05 14:00:00');

INSERT INTO tb_question(question_id, title, difficulty, time_limit, space_limit, content, question_case, default_code, main_fuc, create_by, create_time)
VALUES (6, '搜索旋转排序数组', 3, 3000, 512, '假设有一个排序的按未知的旋转轴旋转的数组所给出的数字。你的函数应该能够搜索某个给定的目标值。如果在数组中找到目标值，则返回它的索引，否则返回 -1。', '[4,5,6,7,0,1,2], 0', 'public class Solution {\n    public int search(int[] nums, int target) {\n        // 在这里编写你的代码\n    }\n}', 'public static void main(String[] args) {\n    Solution solution = new Solution();\n    int[] nums = {4,5,6,7,0,1,2};\n    System.out.println(solution.search(nums, 0));\n}', 1, '2025-01-06 15:00:00');


--竞赛初始数据
INSERT INTO tb_exam (exam_id, title, start_time, end_time, status, create_by, create_time, update_by, update_time)
VALUES (1, '数学竞赛', '2025-01-20 10:00:00', '2025-01-20 12:00:00', 1, 1, NOW(), NULL, NULL);

INSERT INTO tb_exam (exam_id, title, start_time, end_time, status, create_by, create_time, update_by, update_time)
VALUES (2, '物理挑战赛', '2025-02-01 14:00:00', '2025-02-01 16:00:00', 0, 1, NOW(), NULL, NULL);

INSERT INTO tb_exam (exam_id, title, start_time, end_time, status, create_by, create_time, update_by, update_time)
VALUES (3, '化学大赛', '2025-03-05 09:30:00', '2025-03-05 11:30:00', 1, 1, NOW(), NULL, NULL);

INSERT INTO tb_exam (exam_id, title, start_time, end_time, status, create_by, create_time, update_by, update_time)
VALUES (4, '编程马拉松', '2025-04-10 08:00:00', '2025-04-10 17:00:00', 0, 1, NOW(), NULL, NULL);

INSERT INTO tb_exam (exam_id, title, start_time, end_time, status, create_by, create_time, update_by, update_time)
VALUES (5, '生物学联赛', '2025-05-15 13:00:00', '2025-05-15 15:00:00', 1, 1, NOW(), NULL, NULL);

INSERT INTO tb_exam (exam_id, title, start_time, end_time, status, create_by, create_time, update_by, update_time)
VALUES (6,