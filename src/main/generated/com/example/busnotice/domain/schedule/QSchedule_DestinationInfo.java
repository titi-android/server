package com.example.busnotice.domain.schedule;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSchedule_DestinationInfo is a Querydsl query type for DestinationInfo
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSchedule_DestinationInfo extends BeanPath<Schedule.DestinationInfo> {

    private static final long serialVersionUID = 572010681L;

    public static final QSchedule_DestinationInfo destinationInfo = new QSchedule_DestinationInfo("destinationInfo");

    public final StringPath busStopName = createString("busStopName");

    public final StringPath nodeId = createString("nodeId");

    public final StringPath regionName = createString("regionName");

    public QSchedule_DestinationInfo(String variable) {
        super(Schedule.DestinationInfo.class, forVariable(variable));
    }

    public QSchedule_DestinationInfo(Path<? extends Schedule.DestinationInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSchedule_DestinationInfo(PathMetadata metadata) {
        super(Schedule.DestinationInfo.class, metadata);
    }

}

