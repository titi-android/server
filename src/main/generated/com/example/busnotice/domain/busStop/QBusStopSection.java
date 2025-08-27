package com.example.busnotice.domain.busStop;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBusStopSection is a Querydsl query type for BusStopSection
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBusStopSection extends EntityPathBase<BusStopSection> {

    private static final long serialVersionUID = 1147463218L;

    public static final QBusStopSection busStopSection = new QBusStopSection("busStopSection");

    public final ListPath<com.example.busnotice.domain.bus.Bus, com.example.busnotice.domain.bus.QBus> busList = this.<com.example.busnotice.domain.bus.Bus, com.example.busnotice.domain.bus.QBus>createList("busList", com.example.busnotice.domain.bus.Bus.class, com.example.busnotice.domain.bus.QBus.class, PathInits.DIRECT2);

    public final StringPath busStopName = createString("busStopName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nodeId = createString("nodeId");

    public final StringPath regionName = createString("regionName");

    public QBusStopSection(String variable) {
        super(BusStopSection.class, forVariable(variable));
    }

    public QBusStopSection(Path<? extends BusStopSection> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBusStopSection(PathMetadata metadata) {
        super(BusStopSection.class, metadata);
    }

}

