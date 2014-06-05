package com.douwe.banque.dao;

import com.douwe.banque.data.Account;
import java.util.List;

/**
 *
 * @author Vincent Douwe <douwevincent@yahoo.fr>
 */
public interface IAccountDao {

    public Account save(Account account) throws DataAccessException;
    
    public void delete(Account account) throws DataAccessException;
    
    public Account update(Account account) throws DataAccessException;
    
    public List<Account> findAll() throws DataAccessException;
    
    public Account findById(Integer id) throws DataAccessException;

    public Account findByAccountNumber(String accountNumber) throws DataAccessException;

    public List<Account> findByCustomerId(Integer id) throws DataAccessException;
    
}
