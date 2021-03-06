package org.gluu.oxtrust.api.openidconnect;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.OxAuthScope;
import org.gluu.oxtrust.util.OxTrustApiConstants;
import org.slf4j.Logger;
import org.xdi.model.GluuAttribute;

import com.wordnik.swagger.annotations.ApiOperation;

@Path(OxTrustApiConstants.BASE_API_URL + OxTrustApiConstants.SCOPES)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ScopeWebResource extends BaseWebResource {

	@Inject
	private Logger logger;

	@Inject
	private ScopeService scopeService;
	@Inject
	private AttributeService attributeService;

	public ScopeWebResource() {
	}

	@GET
	@ApiOperation(value = "Get all scopes")
	public Response getAllScopes() {
		log("List openid connect scopes ");
		try {
			return Response.ok(scopeService.searchScopes(null, 100)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Get a specific openid connect scope")
	public Response getScopeByInum(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		log("Get openid connect scope by " + inum);
		try {
			OxAuthScope scope = scopeService.getScopeByInum(inum);
			if (scope != null) {
				return Response.ok(scope).build();
			} else {
				return Response.ok(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.SEARCH)
	@ApiOperation(value = "Search openid connect scopes")
	public Response searchScope(@QueryParam(OxTrustApiConstants.SEARCH_PATTERN) String pattern,
			@DefaultValue("10") @QueryParam("size") int size) {
		log("Search openid connect scopes with pattern= " + pattern);
		try {
			List<OxAuthScope> scopes = scopeService.searchScopes(pattern, size);
			return Response.ok(scopes).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@ApiOperation(value = "Add an openidconnect scope")
	public Response createScope(OxAuthScope scope) {
		log("Create scope");
		try {
			Objects.requireNonNull(scope, "Attempt to create null scope");
			String inum = scopeService.generateInumForNewScope();
			scope.setInum(inum);
			scope.setDn(scopeService.getDnForScope(inum));
			scopeService.addScope(scope);
			return Response.ok(scopeService.getScopeByInum(inum)).build();
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PUT
	@ApiOperation(value = "Update openidconect scope")
	public Response updateScope(OxAuthScope scope) {
		String inum = scope.getInum();
		log("Update scope " + inum);
		try {
			Objects.requireNonNull(scope, "Attempt to update scope null value");
			Objects.requireNonNull(inum);
			OxAuthScope existingScope = scopeService.getScopeByInum(inum);
			if (existingScope != null) {
				scope.setInum(existingScope.getInum());
				scopeService.updateScope(scope);
				return Response.ok(scopeService.getScopeByInum(inum)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path(OxTrustApiConstants.INUM_PARAM_PATH + OxTrustApiConstants.CLAIMS)
	@ApiOperation(value = "List all claims of a scope")
	public Response getScopeClaims(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		log("List all claims of scope ==> " + inum);
		try {
			Objects.requireNonNull(inum, "inum should not be null");
			OxAuthScope oxAuthScope = scopeService.getScopeByInum(inum);
			List<String> claimsDn = new ArrayList<String>();
			List<GluuAttribute> attributes = new ArrayList<GluuAttribute>();
			if (oxAuthScope != null) {
				claimsDn = oxAuthScope.getOxAuthClaims();
				for (String claimDn : claimsDn) {
					attributes.add(attributeService.getAttributeByDn(claimDn));
				}
				return Response.ok(attributes).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path(OxTrustApiConstants.INUM_PARAM_PATH)
	@ApiOperation(value = "Delete an openidconnect scope")
	public Response deleteScope(@PathParam(OxTrustApiConstants.INUM) @NotNull String inum) {
		log("Delete openidconnect scope " + inum);
		try {
			OxAuthScope scope = scopeService.getScopeByInum(inum);
			if (scope != null) {
				scopeService.removeScope(scope);
				return Response.ok().build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			log(logger, e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	public Response deleteScopes() {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

	private void log(String message) {
		logger.debug("################# Request: " + message);
	}

}
