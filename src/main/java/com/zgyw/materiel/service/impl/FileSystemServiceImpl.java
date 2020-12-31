package com.zgyw.materiel.service.impl;


import com.jcraft.jsch.*;
import com.zgyw.materiel.config.SftpConfig;
import com.zgyw.materiel.enums.ResultEnum;
import com.zgyw.materiel.exception.MTException;
import com.zgyw.materiel.service.FileSystemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

@Service
@Slf4j
public class FileSystemServiceImpl implements FileSystemService {

    @Autowired
    private SftpConfig sftpConfig;

    private static final String SESSION_CONFIG_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    /**
     * 创建SFTP连接
     *
     * @return
     * @throws Exception
     */
    private ChannelSftp createSftp() throws Exception {
        JSch jsch = new JSch();

        Session session = createSession(jsch, sftpConfig.getHost(), sftpConfig.getUsername(), sftpConfig.getPort());
        session.setPassword(sftpConfig.getPassword());
        session.connect(sftpConfig.getSessionConnectTimeout());

        log.info("Session connected to {}.", sftpConfig.getHost());

        Channel channel = session.openChannel(sftpConfig.getProtocol());
        channel.connect(sftpConfig.getChannelConnectedTimeout());

        log.info("Channel created to {}.", sftpConfig.getHost());

        return (ChannelSftp) channel;
    }

    /**
     * 创建session
     *
     * @param jsch
     * @param host
     * @param username
     * @param port
     * @return
     * @throws Exception
     */
    private Session createSession(JSch jsch, String host, String username, Integer port) throws Exception {
        Session session = null;
        if (port <= 0) {
            session = jsch.getSession(username, host);
        } else {
            session = jsch.getSession(username, host, port);
        }
        if (session == null) {
            throw new Exception(host + " session is null");
        }
        session.setConfig(SESSION_CONFIG_STRICT_HOST_KEY_CHECKING, sftpConfig.getSessionStrictHostKeyChecking());
        return session;
    }

    /**
     * 关闭连接
     *
     * @param sftp
     */
    private void disconnect(ChannelSftp sftp) {
        try {
            if (sftp != null) {
                if (sftp.isConnected()) {
                    sftp.disconnect();
                } else if (sftp.isClosed()) {
                    log.info("sftp is closed already");
                }
                if (null != sftp.getSession()) {
                    sftp.getSession().disconnect();
                }
            }
        } catch (JSchException e) {
            log.error(getClass().getName(), e);
            throw new MTException(ResultEnum.FAIL);
        }
    }

    @Override
    public String uploadFile(String targetPath, InputStream inputStream) throws Exception {
        ChannelSftp sftp = this.createSftp();
        try {
            sftp.cd(sftpConfig.getRoot());
            int index = targetPath.lastIndexOf("/");
            String fileDir = targetPath.substring(0, index);
            String fileName = targetPath.substring(index + 1);
            boolean dirs = this.createDirs(fileDir, sftp);
            if (!dirs) {
                log.error("Remote path error. path:{}", targetPath);
                throw new Exception("Upload File failure");
            }
            sftp.put(inputStream, fileName);
            return sftpConfig.getRoot() + targetPath;
        } catch (Exception e) {
            log.error("Upload file failure. TargetPath: {}", targetPath, e);
            throw new MTException(ResultEnum.FAIL);
        } finally {
            this.disconnect(sftp);
        }
    }

    private boolean createDirs(String dirPath, ChannelSftp sftp) {
        if (dirPath != null && !dirPath.isEmpty()
                && sftp != null) {
            String[] dirs = Arrays.stream(dirPath.split("/"))
                    .filter(StringUtils::isNotBlank)
                    .toArray(String[]::new);
            for (String dir : dirs) {
                try {
                    sftp.cd(dir);
                    log.info("Change directory {}", dir);
                } catch (Exception e) {
                    try {
                        sftp.mkdir(dir);
                        log.info("Create directory {}", dir);
                    } catch (SftpException e1) {
                        log.error("Create directory failure, directory:{}", dir, e1);
                        throw new MTException(ResultEnum.FAIL);
                    }
                    try {
                        sftp.cd(dir);
                        log.info("Change directory {}", dir);
                    } catch (SftpException e1) {
                        log.error("Change directory failure, directory:{}", dir, e1);
                        throw new MTException(ResultEnum.FAIL);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public File downloadFile(String targetPath) throws Exception {
        ChannelSftp sftp = this.createSftp();
        OutputStream outputStream = null;
        try {
            File file = new File(targetPath.substring(targetPath.lastIndexOf("/") + 1));
            outputStream = new FileOutputStream(file);
            sftp.get(targetPath, outputStream);
            return file;
        } catch (Exception e) {
            log.error("Download file failure. TargetPath: {}", targetPath, e);
            throw new MTException(ResultEnum.FAIL);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            this.disconnect(sftp);
        }
    }

    @Override
    public boolean deleteFile(String targetPath) throws Exception {
        ChannelSftp sftp = null;
        try {
            sftp = this.createSftp();
            sftp.rm(targetPath);
            return true;
        } catch (Exception e) {
            log.error("Delete file failure. TargetPath: {}", targetPath, e);
            throw new MTException(ResultEnum.FAIL);
        } finally {
            this.disconnect(sftp);
        }
    }

    public boolean deleteFiles(String path) throws Exception {
        ChannelSftp sftp = null;
        boolean rel = false;
        try {
            sftp = this.createSftp();
            String[] paths = path.split(",");
            for (String s : paths) {
                sftp.rm(s);
                rel = true;
            }
        } catch (Exception e) {
            rel = false;
            throw new MTException(ResultEnum.FAIL);
        } finally {
            this.disconnect(sftp);
        }
        return rel;
    }
}
