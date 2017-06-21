package com.fiskkit.instantEmail.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {
	@Id
	@SequenceGenerator(name = "pk_sequence", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_sequence")
	Long id;

	@Column(nullable = false, unique = true)
	Integer phpId;

	@Column
	BigDecimal balance = BigDecimal.ZERO;

	@Column
	LocalDateTime addedAt = LocalDateTime.now();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getPhpId() {
		return phpId;
	}

	public void setPhpId(Integer phpUserId) {
		phpId = phpUserId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((phpId == null) ? 0 : phpId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof User)) {
			return false;
		}
		User other = (User) obj;
		if (phpId == null) {
			if (other.phpId != null) {
				return false;
			}
		} else if (!phpId.equals(other.phpId)) {
			return false;
		}
		return true;
	}

  @Override
  public String toString() {
    return "[User: "+this.getId().toString()+" {phpId: "+this.getPhpId()+", "+this.getBalance();
  }
}
