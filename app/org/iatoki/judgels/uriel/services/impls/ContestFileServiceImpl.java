package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.FileInfo;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.uriel.config.ContestFileSystemProvider;
import org.iatoki.judgels.uriel.services.ContestFileService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Singleton
@Named("contestFileService")
public final class ContestFileServiceImpl implements ContestFileService {

    private final FileSystemProvider contestFileSystemProvider;

    @Inject
    public ContestFileServiceImpl(@ContestFileSystemProvider FileSystemProvider contestFileSystemProvider) {
        this.contestFileSystemProvider = contestFileSystemProvider;
    }

    @Override
    public List<FileInfo> getContestFiles(String contestJid) {
        return contestFileSystemProvider.listFilesInDirectory(getContestFileDirPath(contestJid));
    }

    @Override
    public String getContestFileURL(String contestJid, String filename) {
        return contestFileSystemProvider.getURL(getContestFilePath(contestJid, filename));
    }

    @Override
    public void uploadContestFile(String contestJid, File file, String filename) throws IOException {
        contestFileSystemProvider.uploadFile(getContestFileDirPath(contestJid), file, filename);
    }

    private List<String> getContestFileDirPath(String contestJid) {
        return ImmutableList.of(contestJid);
    }

    private List<String> getContestFilePath(String contestJid, String filename) {
        return ImmutableList.of(contestJid, filename);
    }
}
