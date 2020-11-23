package com.diboot.core.binding.parser;

import com.diboot.core.binding.binder.BaseBinder;
import com.diboot.core.util.S;
import com.diboot.core.util.V;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 条件表达式的管理器
 * @author Wujun
 * @version v2.0
 * @date 2019/4/1
 */
public class ConditionManager {
    private static final Logger log = LoggerFactory.getLogger(ConditionManager.class);

    /**
     * 表达式缓存Map
     */
    private static Map<String, List<Expression>> expressionParseResultMap = new ConcurrentHashMap<>();

    /**
     * 获取解析后的Expression列表
     * @param condition
     * @return
     */
    private static List<Expression> getExpressionList(String condition){
        if(V.isEmpty(condition)){
            return null;
        }
        List<Expression> expressionList = expressionParseResultMap.get(condition);
        if(expressionList == null){
            ConditionParser visitor = new ConditionParser();
            try{
                Expression expression = CCJSqlParserUtil.parseCondExpression(condition);
                expression.accept(visitor);
                expressionList = visitor.getExpressList();
                expressionParseResultMap.put(condition, expressionList);
            }
            catch (Exception e){
                log.error("关联条件解析异常", e);
            }
        }
        return expressionList;
    }

    /**
     * 附加条件到binder
     * @param binder
     * @throws Exception
     */
    public static <T> void parseConditions(String condition, BaseBinder<T> binder) throws Exception{
        List<Expression> expressionList = getExpressionList(condition);
        if(V.isEmpty(expressionList)){
            log.warn("无法解析注解条件: {} ", condition);
            return;
        }
        // 解析中间表关联
        String tableName = extractMiddleTableName(expressionList);
        if(tableName != null){
            parseMiddleTable(binder, expressionList, tableName);
        }
        else{
            parseDirectRelation(binder, expressionList);
        }
    }

    /**
     * 解析直接关联
     * @param binder
     * @param expressionList
     * @param <T>
     */
    private static <T> void parseDirectRelation(BaseBinder<T> binder, List<Expression> expressionList) {
        // 解析直接关联
        for(Expression operator : expressionList){
            if(operator instanceof EqualsTo){
                EqualsTo express = (EqualsTo)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.getRightExpression() instanceof Column){
                    String entityColumn = removeLeftAlias(express.getRightExpression().toString());
                    binder.joinOn(annoColumn, entityColumn);
                }
                else{
                    binder.andEQ(annoColumn, express.getRightExpression().toString());
                }
            }
            else if(operator instanceof NotEqualsTo){
                NotEqualsTo express = (NotEqualsTo)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.getRightExpression() instanceof Column){
                    binder.andApply(S.toSnakeCase(annoColumn) + " != " + S.toSnakeCase(express.getRightExpression().toString()));
                }
                else{
                    binder.andNE(annoColumn, express.getRightExpression().toString());
                }
            }
            else if(operator instanceof GreaterThan){
                GreaterThan express = (GreaterThan)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.getRightExpression() instanceof Column){
                    binder.andApply(S.toSnakeCase(annoColumn) + " > "+ S.toSnakeCase(express.getRightExpression().toString()));
                }
                else{
                    binder.andGT(annoColumn, express.getRightExpression().toString());
                }
            }
            else if(operator instanceof GreaterThanEquals){
                GreaterThanEquals express = (GreaterThanEquals)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.getRightExpression() instanceof Column){
                    binder.andApply(S.toSnakeCase(annoColumn) + " >= "+ express.getRightExpression().toString());
                }
                else{
                    binder.andGE(annoColumn, express.getRightExpression().toString());
                }
            }
            else if(operator instanceof MinorThan){
                MinorThan express = (MinorThan)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.getRightExpression() instanceof Column){
                    binder.andApply(S.toSnakeCase(annoColumn) + " < "+ express.getRightExpression().toString());
                }
                else{
                    binder.andLT(annoColumn, express.getRightExpression().toString());
                }
            }
            else if(operator instanceof MinorThanEquals){
                MinorThanEquals express = (MinorThanEquals)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.getRightExpression() instanceof Column){
                    binder.andApply(S.toSnakeCase(annoColumn) + " <= "+ express.getRightExpression().toString());
                }
                else{
                    binder.andLE(annoColumn, express.getRightExpression().toString());
                }
            }
            else if(operator instanceof IsNullExpression){
                IsNullExpression express = (IsNullExpression)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.isNot() == false){
                    binder.andIsNull(annoColumn);
                }
                else{
                    binder.andIsNotNull(annoColumn);
                }
            }
            else if(operator instanceof InExpression){
                InExpression express = (InExpression)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.isNot() == false){
                    binder.andApply(S.toSnakeCase(annoColumn) + " IN " + express.getRightItemsList().toString());
                }
                else{
                    binder.andApply(S.toSnakeCase(annoColumn) + " NOT IN " + express.getRightItemsList().toString());
                }
            }
            else if(operator instanceof Between){
                Between express = (Between)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.isNot() == false){
                    binder.andBetween(annoColumn, express.getBetweenExpressionStart().toString(), express.getBetweenExpressionEnd().toString());
                }
                else{
                    binder.andNotBetween(annoColumn, express.getBetweenExpressionStart().toString(), express.getBetweenExpressionEnd().toString());
                }
            }
            else if(operator instanceof LikeExpression){
                LikeExpression express = (LikeExpression)operator;
                String annoColumn = removeLeftAlias(express.getLeftExpression().toString());
                if(express.isNot() == false){
                    binder.andLike(annoColumn, express.getStringExpression());
                }
                else{
                    binder.andNotLike(annoColumn, express.getStringExpression());
                }
            }
            else{
                log.warn("不支持的条件: "+operator.toString());
            }
        }
    }

    /**
     * 解析中间表
     * @param expressionList
     * @return
     */
    private static <T> void parseMiddleTable(BaseBinder<T> binder, List<Expression> expressionList, String tableName) {
        // 单一条件不是中间表条件
        if(expressionList.size() <= 1){
            return;
        }
        // 提取到表
        MiddleTable middleTable = new MiddleTable(tableName);
        // 中间表两边边连接字段
        String middleTableEqualsToAnnoObjectFKColumn = null, middleTableEqualsToRefEntityPkColumn = null;
        // VO与Entity的关联字段
        String annoObjectForeignKey = null, referencedEntityPrimaryKey = null;
        for(Expression operator : expressionList){
            if(operator instanceof EqualsTo){
                EqualsTo express = (EqualsTo)operator;
                // 均为列
                if(express.getLeftExpression() instanceof Column && express.getRightExpression() instanceof Column){
                    String leftColumn = express.getLeftExpression().toString();
                    String rightColumn = express.getRightExpression().toString();
                    // 如果右侧为中间表字段，如: this.departmentId=Department.id
                    if(rightColumn.startsWith(tableName+".")){
                        // 绑定左手边连接列
                        String leftHandColumn = removeLeftAlias(leftColumn);
                        // this. 开头的vo对象字段
                        if(isVoColumn(leftColumn)){
                            // 识别到vo对象的属性 departmentId
                            annoObjectForeignKey = leftHandColumn;
                            // 对应中间表的关联字段
                            middleTableEqualsToAnnoObjectFKColumn = removeLeftAlias(rightColumn);
                        }
                        else{
                            // 注解关联的entity主键
                            referencedEntityPrimaryKey = leftHandColumn;
                            middleTableEqualsToRefEntityPkColumn = removeLeftAlias(rightColumn);
                        }
                        binder.joinOn(annoObjectForeignKey, referencedEntityPrimaryKey);
                        middleTable.connect(middleTableEqualsToAnnoObjectFKColumn, middleTableEqualsToRefEntityPkColumn);
                    }
                    // 如果左侧为中间表字段，如: Department.orgId=id  (entity=Organization)
                    if(leftColumn.startsWith(tableName+".")){
                        // 绑定右手边连接列
                        String rightHandColumn = removeLeftAlias(rightColumn);
                        if(isVoColumn(rightColumn)){
                            // 识别到vo对象的属性 departmentId
                            annoObjectForeignKey = rightHandColumn;
                            // 对应中间表的关联字段
                            middleTableEqualsToAnnoObjectFKColumn = S.substringAfter(leftColumn, ".");
                        }
                        else{
                            referencedEntityPrimaryKey = rightHandColumn;
                            middleTableEqualsToRefEntityPkColumn = removeLeftAlias(leftColumn);
                        }
                        binder.joinOn(annoObjectForeignKey, referencedEntityPrimaryKey);
                        middleTable.connect(middleTableEqualsToAnnoObjectFKColumn, middleTableEqualsToRefEntityPkColumn);
                    }
                }
                else{ // equals附加条件，暂只支持列在左侧，如 department.level=1
                    String leftExpression = express.getLeftExpression().toString();
                    if(leftExpression != null && leftExpression.startsWith(tableName+".")){
                        middleTable.addAdditionalCondition(removeLeftAlias(operator.toString()));
                    }
                }
            }
            else{
                String leftExpression = null;
                if(operator instanceof NotEqualsTo){
                    NotEqualsTo express = (NotEqualsTo)operator;
                    leftExpression = express.getLeftExpression().toString();
                }
                else if(operator instanceof GreaterThan){
                    GreaterThan express = (GreaterThan)operator;
                    leftExpression = express.getLeftExpression().toString();
                }
                else if(operator instanceof GreaterThanEquals){
                    GreaterThanEquals express = (GreaterThanEquals)operator;
                    leftExpression = express.getLeftExpression().toString();
                }
                else if(operator instanceof MinorThan){
                    MinorThan express = (MinorThan)operator;
                    leftExpression = express.getLeftExpression().toString();
                }
                else if(operator instanceof MinorThanEquals){
                    MinorThanEquals express = (MinorThanEquals)operator;
                    leftExpression = express.getLeftExpression().toString();
                }
                else if(operator instanceof IsNullExpression){
                    IsNullExpression express = (IsNullExpression)operator;
                    leftExpression = express.getLeftExpression().toString();
                }
                else if(operator instanceof InExpression){
                    InExpression express = (InExpression)operator;
                    leftExpression = express.getLeftExpression().toString();
                }
                else if(operator instanceof Between){
                    Between express = (Between)operator;
                    leftExpression = express.getLeftExpression().toString();
                }
                else if(operator instanceof LikeExpression){
                    LikeExpression express = (LikeExpression)operator;
                    leftExpression = express.getLeftExpression().toString();
                }
                if(leftExpression != null && leftExpression.startsWith(tableName+".")){
                    middleTable.addAdditionalCondition(removeLeftAlias(operator.toString()));
                }
            }
        }
        binder.withMiddleTable(middleTable);
    }

    /**
     * 提取中间表表对象名
     * @param expressionList
     * @return
     */
    private static String extractMiddleTableName(List<Expression> expressionList){
        Map<String, Integer> tableNameCountMap = new HashMap<>();
        for(Expression operator : expressionList){
            if(operator instanceof EqualsTo){
                EqualsTo express = (EqualsTo)operator;
                // 均为列
                if(express.getLeftExpression() instanceof Column && express.getRightExpression() instanceof Column){
                    // 统计左侧列中出现的表名
                    String leftColumn = express.getLeftExpression().toString();
                    countTableName(tableNameCountMap, leftColumn);
                    // 统计右侧列中出现的表名
                    String rightColumn = express.getRightExpression().toString();
                    countTableName(tableNameCountMap, rightColumn);
                }
            }
        }
        if(tableNameCountMap.isEmpty()){
            return null;
        }
        String tableName = null;
        int count = 1;
        for(Map.Entry<String, Integer> entry : tableNameCountMap.entrySet()){
            if(entry.getValue() > count){
                count = entry.getValue();
                tableName = entry.getKey();
            }
        }
        return tableName;
    }

    /**
     * 统计表名出现的次数
     * @param tableNameCountMap
     * @param columnStr
     */
    private static void countTableName(Map<String, Integer> tableNameCountMap, String columnStr) {
        if(columnStr.contains(".")){
            // 如果是中间表(非this,self标识的当前表)
            if(!isVoColumn(columnStr)){
                String tempTableName = S.substringBefore(columnStr, ".");
                Integer count = tableNameCountMap.get(tempTableName);
                if(count == null){
                    count = 0;
                }
                count++;
                tableNameCountMap.put(tempTableName, count);
            }
        }
    }

    /**
     * 注解列
     * @return
     */
    private static String removeLeftAlias(String annoColumn){
        if(annoColumn.contains(".")){
            annoColumn = S.substringAfter(annoColumn, ".");
        }
        return annoColumn;
    }

    /**
     * 是否为VO自身属性（以this开头的）
     * @param expression
     * @return
     */
    private static boolean isVoColumn(String expression){
        String tempTableName = S.substringBefore(expression, ".");
        // 如果是中间表(非this,self标识的当前表)
        return "this".equals(tempTableName) || "self".equals(tempTableName);
    }

}
