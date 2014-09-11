package co.codewizards.cloudstore.rest.server.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.codewizards.cloudstore.core.repo.transport.RepoTransport;
import co.codewizards.cloudstore.core.util.AssertUtil;

@Path("_beginPutFile/{repositoryName}")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class BeginPutFileService extends AbstractServiceWithRepoToRepoAuth
{
	private static final Logger logger = LoggerFactory.getLogger(BeginPutFileService.class);

	{
		logger.debug("<init>: created new instance");
	}

	@POST
	@Path("{path:.*}/{isInProgress}")
	public void beginPutFile(@PathParam("path") String path, @PathParam("isInProgress") final String isInProgress)
	{
		AssertUtil.assertNotNull("path", path);
		final RepoTransport repoTransport = authenticateAndCreateLocalRepoTransport();
		try {
			path = repoTransport.unprefixPath(path);
			repoTransport.beginPutFile(path, Boolean.parseBoolean(isInProgress));
		} finally {
			repoTransport.close();
		}
	}
}
