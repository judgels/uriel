package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.FileInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ContestFileService {

    List<FileInfo> getContestFiles(String contestJid);

    String getContestFileURL(String contestJid, String filename);

    void uploadContestFile(String contestJid, File file, String filename) throws IOException;
}
