package org.janus.lib;


import com.jcraft.jsch.Session;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;

import java.io.File;
import java.io.IOException;

public class VersionControlledDirectory {
    private final String path;
    private final Git git;

    private VersionControlledDirectory(String path) throws IOException {
        this.path = path;
        this.git = Git.open(new File(path));
    }

    public static VersionControlledDirectory open(String path) throws IOException {
        return new VersionControlledDirectory(path);
    }

    public String getPath() {
        return path;
    }

    public static VersionControlledDirectory cloneFrom(RemoteDirectory remoteDirectory, String targetDirectory) throws GitAPIException, IOException {
        File targetPath;
        if (new File(targetDirectory).exists()) {
            targetPath = new File(targetDirectory, FilenameUtils.getBaseName(remoteDirectory.getPath()));
            System.out.println(targetPath);
        } else {
            targetPath = new File(targetDirectory);
        }
        System.out.println("Target Path: " + targetPath.getAbsolutePath());
        try {
            TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback(remoteDirectory.getPassword());
            Git.cloneRepository().setDirectory(targetPath).setTransportConfigCallback(transportConfigCallback)
                    .setURI(remoteDirectory.getUri()).call();
        } catch (GitAPIException e) {
            System.out.println("Couldn't clone :" + remoteDirectory.getUri());
            throw e;
        }
        return new VersionControlledDirectory(targetPath.getAbsolutePath());
    }

    public static void main(String[] args) throws GitAPIException, IOException {
        RemoteDirectory remoteDirectory = new RemoteDirectory("c13u06.int.janelia.org",
                "/groups/scicompsoft/home/zouinkhim/test_versioned")
                .setUsername("zouinkhim")
                .setPassword(args[0].getBytes());
        System.out.println(remoteDirectory.getUri());

        VersionControlledDirectory versionControlledDirectory = VersionControlledDirectory.cloneFrom(remoteDirectory, new File("").getAbsolutePath());
        System.out.println(versionControlledDirectory.getPath());

    }

    private static class SshTransportConfigCallback implements TransportConfigCallback {
        private final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(password);
            }
        };
        private final byte[] password;

        public SshTransportConfigCallback(byte[] password) {
            this.password = password;
        }

        @Override
        public void configure(Transport transport) {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }
    }
}
