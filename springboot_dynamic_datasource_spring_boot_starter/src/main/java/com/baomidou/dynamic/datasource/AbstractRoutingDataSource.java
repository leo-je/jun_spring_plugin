/**
 * Copyright © 2018 organization baomidou
 * <pre>
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
 * <pre/>
 */
package com.baomidou.dynamic.datasource;

import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import lombok.Data;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * 抽象动态获取数据源
 *
 * @author Wujun
 * @since 2.2.0
 */
public abstract class AbstractRoutingDataSource extends AbstractDataSource {

    /**
     * 子类实现决定最终数据源
     *
     * @return 数据源
     */
    protected abstract DataSource determineDataSource();

    @Override
    public Connection getConnection() throws SQLException {
        return determineDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineDataSource().getConnection(username, password);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineDataSource().isWrapperFor(iface));
    }

    @Data
    protected static class GroupDataSource {

        private String groupName;

        private DynamicDataSourceStrategy dynamicDataSourceStrategy;

        private List<DataSource> dataSources = new LinkedList<>();

        public GroupDataSource(String groupName, DynamicDataSourceStrategy dynamicDataSourceStrategy) {
            this.groupName = groupName;
            this.dynamicDataSourceStrategy = dynamicDataSourceStrategy;
        }

        public void addDatasource(DataSource dataSource) {
            dataSources.add(dataSource);
        }

        public void removeDatasource(DataSource dataSource) {
            dataSources.remove(dataSource);
        }

        public DataSource determineDataSource() {
            return dynamicDataSourceStrategy.determineDataSource(dataSources);
        }

        public int size() {
            return dataSources.size();
        }
    }
}
