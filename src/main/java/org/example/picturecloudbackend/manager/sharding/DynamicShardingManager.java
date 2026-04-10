package org.example.picturecloudbackend.manager.sharding;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.example.picturecloudbackend.model.entity.Space;
import org.example.picturecloudbackend.model.enums.SpaceTypeEnum;
import org.example.picturecloudbackend.service.SpaceService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
//@Component
public class DynamicShardingManager {
    @Resource
    private DataSource dataSource;

    @Resource
    private SpaceService spaceService;

    private static final String DATABASE_NAME = "picture_cloud";

    private static final String LOGIC_TABLE = "picture";

    private static final String LOGIC_TABLE_FORMAT = "picture_%d";


    @PostConstruct
    public void initialize(){
        updateShardingTableNodes();
    }

    private Set<String> fetchAllPictureTableNames() {
        Set<Long> spaceIds = spaceService.lambdaQuery().eq(Space::getSpaceType, SpaceTypeEnum.TEAM).list().stream().map(Space::getId).collect(Collectors.toSet());
        Set<String> tableNames = spaceIds.stream().map(spaceId -> String.format(LOGIC_TABLE_FORMAT, spaceId)).collect(Collectors.toSet());
        tableNames.add(LOGIC_TABLE);
        return tableNames;
    }

    private ContextManager getContextManager() {
        try (ShardingSphereConnection connection = dataSource.getConnection().unwrap(ShardingSphereConnection.class)) {
            return connection.getContextManager();
        } catch (SQLException e) {
            throw new RuntimeException("获取ShardingSphere ContextManager失败", e);
        }
    }

    private void updateShardingTableNodes() {
        Set<String> tableNames = fetchAllPictureTableNames();
        String newActualDataNodes = tableNames.stream().map(tableName -> String.format("picture_cloud.%s", tableName)).collect(Collectors.joining(","));
        log.info("动态分表的actual-data-nodes配置:{}", newActualDataNodes);
        // 获取ShardingSphere的上下文对象
        ContextManager contextManager = getContextManager();
        ShardingSphereRuleMetaData ruleMetaData = contextManager.getMetaDataContexts().getMetaData()
                .getDatabases()
                .get(DATABASE_NAME)
                .getRuleMetaData();

        Optional<ShardingRule> shardingRule = ruleMetaData.findSingleRule(ShardingRule.class);
        if (shardingRule.isPresent()) {
            ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration) shardingRule.get().getConfiguration();
            List<ShardingTableRuleConfiguration> updateRules = ruleConfig.getTables()
                    .stream()
                    .map(oldTableRuleConfig -> {
                        if(Objects.equals(LOGIC_TABLE, oldTableRuleConfig.getLogicTable())){
                            ShardingTableRuleConfiguration newTableRuleConfig = new ShardingTableRuleConfiguration(LOGIC_TABLE, newActualDataNodes);
                            newTableRuleConfig.setDatabaseShardingStrategy(oldTableRuleConfig.getDatabaseShardingStrategy());
                            newTableRuleConfig.setTableShardingStrategy(oldTableRuleConfig.getTableShardingStrategy());
                            newTableRuleConfig.setKeyGenerateStrategy(oldTableRuleConfig.getKeyGenerateStrategy()); // 主键生成策略
                            newTableRuleConfig.setAuditStrategy(oldTableRuleConfig.getAuditStrategy()); // 审计策略
                            return newTableRuleConfig;
                        }
                        return oldTableRuleConfig;
                    }).collect(Collectors.toList());
            ruleConfig.setTables(updateRules);
            contextManager.alterRuleConfiguration(DATABASE_NAME, Collections.singleton(ruleConfig));
            contextManager.reloadDatabase(DATABASE_NAME);
            log.info("动态分表规则更新成功!");
        }else{
            log.info("未找到动态分表规则,动态分表失败");
        }

    }
}
