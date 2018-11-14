/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler;

import java.util.Collection;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.PrimaryKeyExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import lombok.RequiredArgsConstructor;

/**
 * Add primary key extract handler.
 * 
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class AddPrimaryKeyExtractHandler implements ASTExtractHandler, ASTExtractHandler1 {
    
    private final RuleName ruleName;
    
    @Override
    public void extract(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        Optional<ParserRuleContext> modifyColumnNode = ASTUtils.findFirstChildNode(ancestorNode, ruleName);
        if (!modifyColumnNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> primaryKeyNode = ASTUtils.findFirstChildNode(modifyColumnNode.get(), RuleName.PRIMARY_KEY);
        if (!primaryKeyNode.isPresent()) {
            return;
        }
        for (ParserRuleContext each : ASTUtils.getAllDescendantNodes(modifyColumnNode.get(), RuleName.COLUMN_NAME)) {
            String columnName = each.getText();
            Optional<ColumnDefinition> updateColumn = alterStatement.getColumnDefinitionByName(columnName);
            if (updateColumn.isPresent()) {
                updateColumn.get().setPrimaryKey(true);
                alterStatement.getUpdateColumns().put(columnName, updateColumn.get());
            }
        }
    }

    @Override
    public ExtractResult extract(ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> modifyColumnNode = ASTUtils.findFirstChildNode(ancestorNode, ruleName);
        if (!modifyColumnNode.isPresent()) {
            return null;
        }
        Optional<ParserRuleContext> primaryKeyNode = ASTUtils.findFirstChildNode(modifyColumnNode.get(), RuleName.PRIMARY_KEY);
        if (!primaryKeyNode.isPresent()) {
            return null;
        }
        Collection<ParserRuleContext> result = ASTUtils.getAllDescendantNodes(modifyColumnNode.get(), RuleName.COLUMN_NAME);
        if(null == result) {
            return null;
        }
        PrimaryKeyExtractResult extractResult = new PrimaryKeyExtractResult();
        for (ParserRuleContext each : result) {
            extractResult.getPrimaryKeyColumnNames().add(each.getText());
        }
        return extractResult;
    }
}
