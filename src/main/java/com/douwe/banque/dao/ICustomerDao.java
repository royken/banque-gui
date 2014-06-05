package com.douwe.banque.dao;

import com.douwe.banque.data.Customer;
import java.util.List;

/**
 *
 * @author Vincent Douwe <douwevincent@yahoo.fr>
 */
public interface ICustomerDao {
 
    public Customer save(Customer customer) throws DataAccessException;
    
    public void delete(Customer customer) throws DataAccessException;
    
    public Customer update(Customer customer) throws DataAccessException;
    
    public Customer findById(Integer id) throws DataAccessException;
    
    public List<Customer> findAll() throws DataAccessException;

    public Customer findByLogin(String login) throws DataAccessException;

    public Customer findByName(String customer)  throws DataAccessException;
}
