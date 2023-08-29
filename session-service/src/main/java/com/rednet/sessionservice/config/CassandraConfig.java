package com.rednet.sessionservice.config;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;

import java.util.List;


@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {
    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspace;

    @Value("${spring.cassandra.schema-action}")
    private SchemaAction schemaAction;

    @Override
    @Nonnull
    protected String getContactPoints() {
        return contactPoints;
    }

    @Override
    @Nonnull
    public SchemaAction getSchemaAction() {
        return schemaAction;
    }

    @Override
    @Nonnull
    public String getKeyspaceName() {
        return keyspace;
    }

    @Override
    @Nonnull
    public String[] getEntityBasePackages() {
        return new String[]{"com.rednet"};
    }

    @Override
    @Nonnull
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        final CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(keyspace)
                .ifNotExists()
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withSimpleReplication();

        return List.of(specification);
    }
}
