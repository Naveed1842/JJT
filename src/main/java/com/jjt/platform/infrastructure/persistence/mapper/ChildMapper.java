package com.jjt.platform.infrastructure.persistence.mapper;

import com.jjt.platform.core.domain.entity.Child;
import com.jjt.platform.core.domain.value.Money;
import com.jjt.platform.infrastructure.persistence.entity.ChildEntity;

import java.util.Objects;

public final class ChildMapper {

    private ChildMapper() {}

    public static Child toDomain(ChildEntity entity) {
        Objects.requireNonNull(entity, "entity");
        Money cost = MoneyMapper.toDomain(entity.getEducationAmount(), entity.getEducationCurrency());
        return new Child(
                entity.getId(),
                entity.getFullName(),
                cost,
                entity.getRollNumber(),
                entity.getCity(),
                entity.getCampusName(),
                entity.getSchoolName());
    }

    public static ChildEntity toEntity(Child child) {
        Objects.requireNonNull(child, "child");
        return ChildEntity.create(
                child.getId(),
                child.getFullName(),
                MoneyMapper.amount(child.getEducationCost()),
                MoneyMapper.currency(child.getEducationCost()),
                child.getRollNumber(),
                child.getCity(),
                child.getCampusName(),
                child.getSchoolName()
        );
    }
}
