package com.example.busnotice.domain.subway;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSubwaySection is a Querydsl query type for SubwaySection
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubwaySection extends EntityPathBase<SubwaySection> {

    private static final long serialVersionUID = -915349446L;

    public static final QSubwaySection subwaySection = new QSubwaySection("subwaySection");

    public final StringPath dir = createString("dir");

    public final StringPath dirName = createString("dirName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lineName = createString("lineName");

    public final StringPath regionName = createString("regionName");

    public final StringPath stationName = createString("stationName");

    public QSubwaySection(String variable) {
        super(SubwaySection.class, forVariable(variable));
    }

    public QSubwaySection(Path<? extends SubwaySection> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSubwaySection(PathMetadata metadata) {
        super(SubwaySection.class, metadata);
    }

}

