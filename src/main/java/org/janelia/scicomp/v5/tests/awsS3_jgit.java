/*
 * *
 *  * Copyright (c) 2022, Janelia
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice,
 *  *    this list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.janelia.scicomp.v5.tests;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;


// following tuto https://fancybeans.com/2012/08/24/how-to-use-s3-as-a-private-git-repository/
// works using terminal
//.jgit_s3_public
// accesskey: [access ID for AWS]
//secretkey: [secret key for AWS]
//acl: private
// todo implement this commands
//   git remote add origin amazon-s3://.jgit_s3_public@kleio-test-git/projects/hello
// 1052  git push --set-upstream origin main
public class awsS3_jgit {
    public static void main(String[] args) throws IOException, GitAPIException, URISyntaxException {

        String path = "/Users/zouinkhim/Desktop/tmp/test2_java";
        Git git = Git.open(new File(path));
        RemoteAddCommand remoteAdd = git.remoteAdd();
        remoteAdd.setName("origin");
        URIish urish = new URIish("amazon-s3://.jgit_s3_public@kleio-test-git/projects/hello");
        remoteAdd.setUri(urish);
        remoteAdd.call();
//        git.getRepository().getConfig().setString();
        PushCommand pushCommand = git.push();
//        pushCommand.setRemote("s3");
//        pushCommand.setRefSpecs(new RefSpec("refs/heads/master", RefSpec.WildcardMode.REQUIRE_MATCH))
//        pushCommand.setTransp
        System.out.println(pushCommand.getRemote());
        pushCommand.call();
    }
}
