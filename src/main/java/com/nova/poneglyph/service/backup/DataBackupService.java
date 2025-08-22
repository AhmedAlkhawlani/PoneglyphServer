package com.nova.poneglyph.service.backup;



import com.nova.poneglyph.config.BackupConfig;
import com.nova.poneglyph.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DataBackupService {

    private final BackupConfig backupConfig;
    private final EncryptionUtil encryptionUtil;

    @Scheduled(cron = "0 0 2 * * ?") // كل يوم في 2 صباحًا
    public void performBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupName = "backup_" + timestamp + ".zip";
            Path backupPath = Paths.get(backupConfig.getDirectory(), backupName);

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(backupPath))) {
                // أضف ملفات النسخ الاحتياطي هنا
                addDirectoryToZip(zos, Paths.get(backupConfig.getDataDirectory()));

                if (backupConfig.isEncrypt()) {
                    encryptBackup(backupPath);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Backup failed", e);
        }
    }

    private void addDirectoryToZip(ZipOutputStream zos, Path path) throws IOException {
        Files.walk(path)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        ZipEntry zipEntry = new ZipEntry(path.relativize(file).toString());
                        zos.putNextEntry(zipEntry);
                        Files.copy(file, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void encryptBackup(Path backupPath) throws IOException {
//        byte[] data = Files.readAllBytes(backupPath);
//        byte[] encrypted = encryptionUtil.encrypt(data, backupConfig.getEncryptionKey());
//        Files.write(backupPath, encrypted);
    }
}
