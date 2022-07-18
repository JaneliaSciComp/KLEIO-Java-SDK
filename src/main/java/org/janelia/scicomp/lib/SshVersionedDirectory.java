package org.janelia.scicomp.lib;

import com.jcraft.jsch.Session;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;

import java.io.File;
import java.io.IOException;

public class SshVersionedDirectory extends VersionedDirectory {
    private byte[] password;

    public SshVersionedDirectory(String path) throws IOException {
        super(path);
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    private static void executeCommand(TransportCommand command, byte[] password) throws GitAPIException {
        TransportConfigCallback transportConfigCallback = new SSHTransportConfigCallback(password);
        command.setTransportConfigCallback(transportConfigCallback);
        command.call();
    }

    public static SshVersionedDirectory cloneFrom(RemoteDirectory remoteDirectory, String targetDirectory) throws GitAPIException, IOException {
        File targetPath;
        if (new File(targetDirectory).exists())
            targetPath = new File(targetDirectory, FilenameUtils.getBaseName(remoteDirectory.getPath()));
        else
            targetPath = new File(targetDirectory);

        System.out.println("Target Path: " + targetPath.getAbsolutePath());
        try {
            TransportConfigCallback transportConfigCallback = new SSHTransportConfigCallback(remoteDirectory.getPassword());
            Git git = Git.cloneRepository().setDirectory(targetPath).setTransportConfigCallback(transportConfigCallback)
                    .setURI(remoteDirectory.getUri()).call();
            StoredConfig cfg = git.getRepository().getConfig();
            cfg.setString("user", null, "name", remoteDirectory.getUsername());
            cfg.save();
        } catch (GitAPIException e) {
            System.out.println("Couldn't clone :" + remoteDirectory.getUri());
            throw e;
        }
        return new SshVersionedDirectory(targetPath.getAbsolutePath());
    }

    @Override
    public void push() throws GitAPIException {
        if (password == null)
            throw new IllegalStateException("You need to set the user password before");
        PushCommand pushCommand = git.push();
        executeCommand(pushCommand, password);
    }

    @Override
    public void pull() throws GitAPIException {
        if (password == null)
            throw new IllegalStateException("You need to set the user password before");
        PullCommand pullCommand = git.pull();
        executeCommand(pullCommand, password);
    }

    private static class SSHTransportConfigCallback implements TransportConfigCallback {
        private final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(password);
            }
        };
        private final byte[] password;

        public SSHTransportConfigCallback(byte[] password) {
            this.password = password;
        }

        @Override
        public void configure(Transport transport) {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }
    }

    public static void main(String[] args) throws GitAPIException, IOException {
        RemoteDirectory remoteDirectory = new RemoteDirectory("c13u06.int.janelia.org",
                "/groups/scicompsoft/home/zouinkhim/test_versioned")
                .setUsername("zouinkhim")
                .setPassword(args[0].getBytes());
        System.out.println(remoteDirectory.getUri());

        SshVersionedDirectory versionControlledDirectory = SshVersionedDirectory.cloneFrom(remoteDirectory, new File("").getAbsolutePath());
        System.out.println(versionControlledDirectory.getPath());
    }
}
