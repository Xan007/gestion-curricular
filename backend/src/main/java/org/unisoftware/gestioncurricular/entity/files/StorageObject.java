package org.unisoftware.gestioncurricular.entity.files;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "objects", schema = "storage")
@Getter
@Setter
public class StorageObject {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "bucket_id")
    private String bucketId;

    @Column(name = "name")
    private String name;

    // otros campos si los necesitas...

}
