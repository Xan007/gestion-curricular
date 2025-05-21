package org.unisoftware.gestioncurricular.repository.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.files.StorageObject;

import java.util.Optional;
import java.util.UUID;

public interface StorageObjectRepository extends JpaRepository<StorageObject, UUID> {

    Optional<StorageObject> findById(UUID id);

}
