package org.janus.lib;


import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;

import java.io.File;

public class VersionControlledDirectory {

    private static final String REMOTE_URL = "zouinkhim@c13u06.int.janelia.org:/groups/scicompsoft/home/zouinkhim/test_versioned";
    final String path;

    public VersionControlledDirectory(String path) {
        this.path = path;
        if (new File(path).exists()){

        }
    }

    public static void main(String[] args) throws GitAPIException {
        byte[] password = args[0].getBytes();
        TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback(password);
        Git git = Git.cloneRepository().setDirectory(new File("./hello")).setTransportConfigCallback(transportConfigCallback)
                .setURI(REMOTE_URL).call();
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

    private void init() throws GitAPIException {
        Git git = Git.init().setDirectory(new File(path)).call();
    }
}
