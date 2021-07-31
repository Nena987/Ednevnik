package com.iktpreobuka.ednevnik.config;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

	private String secretKey;

	public JWTAuthorizationFilter(String secretKey) {
		super();
		this.secretKey = secretKey;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (checkIfJWTExists(request)) {
			Claims claims = validateToken(request);
			if (claims.get("authorities") != null) {
				setUpSpringAuthentication(claims);
			} else {
				SecurityContextHolder.clearContext();
			}
		} else {
			SecurityContextHolder.clearContext();
		}
		filterChain.doFilter(request, response);
	}

	private void setUpSpringAuthentication(Claims claims) {
		@SuppressWarnings("unchecked")
		List<String> authorities = (List<String>) claims.get("authorities");
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
				authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private Claims validateToken(HttpServletRequest request) {
		String token = request.getHeader("Authorization").replace("Bearer ", "");
		return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
	}

	private boolean checkIfJWTExists(HttpServletRequest request) {
		//
		String token = request.getHeader("Authorization");
		if (token == null || !token.startsWith("Bearer ")) {
			return false;
		}
		return true;
	}

}
