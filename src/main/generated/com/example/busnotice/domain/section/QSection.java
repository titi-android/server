package com.example.busnotice.domain.section;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSection is a Querydsl query type for Section
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSection extends EntityPathBase<Section> {

    private static final long serialVersionUID = -1845193031L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSection section = new QSection("section");

    public final com.example.busnotice.domain.busStop.QBusStopSection busStopSection;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> orderIndex = createNumber("orderIndex", Integer.class);

    public final com.example.busnotice.domain.schedule.QSchedule schedule;

    public final com.example.busnotice.domain.subway.QSubwaySection subwaySection;

    public final StringPath type = createString("type");

    public QSection(String variable) {
        this(Section.class, forVariable(variable), INITS);
    }

    public QSection(Path<? extends Section> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSection(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSection(PathMetadata metadata, PathInits inits) {
        this(Section.class, metadata, inits);
    }

    public QSection(Class<? extends Section> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.busStopSection = inits.isInitialized("busStopSection") ? new com.example.busnotice.domain.busStop.QBusStopSection(forProperty("busStopSection")) : null;
        this.schedule = inits.isInitialized("schedule") ? new com.example.busnotice.domain.schedule.QSchedule(forProperty("schedule"), inits.get("schedule")) : null;
        this.subwaySection = inits.isInitialized("subwaySection") ? new com.example.busnotice.domain.subway.QSubwaySection(forProperty("subwaySection")) : null;
    }

}

