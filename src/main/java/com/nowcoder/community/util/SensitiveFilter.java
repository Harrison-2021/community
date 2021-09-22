package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 定义好前缀树树结构内部类
    private class TrieNode {
        // 关键词结束语标识
        private boolean isKeywordEnd = false;
        // 指向子节点的索引，注意是个map，key为字符指针指向，value为对应的节点，
        // 节点本身是不存储值的，代表的字符已经存储在上级的索引key中
        private Map<Character, TrieNode> subNodes;

        public TrieNode() {
            this.isKeywordEnd = false;
            subNodes = new HashMap<>();
        }

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSunNode(Character c) {
            return subNodes.get(c);
        }
    }

    // 定义好替换符
    private static final String REPLACEMENT = "***";

    // 定义好根节点
    private TrieNode root = new TrieNode();

    // 用SpringMVC管理类的初始化,自动初始化，并且只初始化一次
    @PostConstruct
    public void init() {
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode preNode = root;
        for(int i = 0; i < keyword.length(); i ++) {
            char c = keyword.charAt(i);
            // 先查找根据父亲节点找到的当前节点是否为空，若为空，需要添加，若不为空，表明已经存在，只需继续下轮操作
            TrieNode subNode = preNode.getSunNode(c);
            if(subNode == null) {
                // 添加节点
                subNode = new TrieNode();
                preNode.addSubNode(c, subNode);
            }

            // 指针下移，继续下一轮的循环操作
            preNode = subNode;

            // 当字符串添加完了后，要设置结束标识
            if(i == keyword.length() - 1) {
                preNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词操作
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if(StringUtils.isBlank(text)) {
            return null;
        }

        // 定义返回的文本字符串
        StringBuilder res = new StringBuilder();

        // 定义指针1，指向trie树
        TrieNode preNode = root;
        // 定义指针2，指向待处理文本的字符，后指针，当遇到敏感字符时停下处理，指向的是字符串的开始字符
        int begin = 0;
        // 定义指针3，指向待处理文本的字符，前指针，当遇到敏感字符时，继续向前探索
        int front = 0;

        // 循环结束标识以指针3指向最后一个字符，这样可以减少循环次数，因为指针3最先到达
        while (front < text.length()) {
            char c = text.charAt(front);

            // 要先判断c是否是非常规字符，如何是，要先跳过，敏感词只判断常规字符组成的字符串
            if(isSymbol(c)) {
                // 若指针1处于根节点，即还没有开始判断，特殊字符只是在敏感是前面，不影响敏感词结构，可以将符号计入结果，指针2可以继续走
                if(preNode == root) {
                    res.append(c);
                    begin ++;
                }
                // 无论符号在开头还是中间，指针3都要走一步，因为指针3是探索者，直到有标志位或是空字符才停止回到指针2的位置
                front ++;
                continue;
            }

            // 正常字符，要检查敏感树，即检查敏感树的下级节点
            // 为了让指针1一直走下去，可以将preNode覆盖，指向下级节点，这样可以省略一步
            preNode = preNode.getSunNode(c);
            if(preNode == null) { // 当前节点为空，说明上级节点的索引中没有当前敏感词的字符key，即此字符串不是敏感词
                // 既然查到不是敏感词，以bank为开头到front的字符组成的字符串不是敏感词，bank所指为开始字符，一定不是敏感词组成字符，要输出到res中，
                // back要走一步进行下一轮判断
                res.append(text.charAt(begin));
                // front指针要回到begin处，继续探索
                front = ++begin;
                // 要判断新的字符串，指针1要回到根节点初始位置
                preNode = root;
            } else if (preNode.isKeywordEnd()) { // 如果遇到了单词结束标志，说明当前字符串就是一个完整的敏感词，
                // 要将begin到front字符串替换掉
                res.append(REPLACEMENT);
                // 替换的逻辑是，指针2，3直接跳过敏感词，将替换字符输出到结果中，继续判断
                begin = ++front;
                // 同样，指针1归位，继续判断下一轮
                preNode = root;
            } else { // 最后一种情况，即没有走完敏感词一个分支，也没有遇到敏感词结束标志，front需要继续走下去判断
                front ++;
            }
        }

        // 注意，front走到最后一个字符时，若是敏感词标志位，res已经处理了，若不是，说明就不是敏感词，要将最后begin到front字符输出
        res.append(text.substring(begin));

        return res.toString();
    }

    // 判断是否为正常字符，即是否为符号
    private boolean isSymbol(char c) {
        // 0x2E80~0x9FFF 是东亚文字范围，要排除在外
        return !CharUtils.isAsciiAlpha(c) && (c < 0x2e80 || c > 0x9fff);
    }

}
