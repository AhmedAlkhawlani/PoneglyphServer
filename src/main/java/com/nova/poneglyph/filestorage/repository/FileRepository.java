package com.nova.poneglyph.filestorage.repository;

import com.nova.poneglyph.filestorage.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, String> {
}
