package edu.sraikar.ws.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.sraikar.ws.model.Greeting;
import edu.sraikar.ws.repository.GreetingRepository;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true) // this
																	// indicates
																	// the
																	// service
// methods exposed in
// GreetingService interface
// support existing
// transaction and will not
// start any new transaction
// if any transaction
// already exists and readOnly is set to true to indicate the methods do not
// modify or create data (applies only to findAll() and findOne())

public class GreetingServiceBean implements GreetingService {

	@Autowired
	private GreetingRepository greetingRepository;

	@Autowired
	private CounterService counterService;
	
	@Override
	public Collection<Greeting> findAll() {
		counterService.increment("method.invoked.greetingServiceBean.findAll");
		Collection<Greeting> greetings = greetingRepository.findAll();
		return greetings;
	}

	@Override
	@Cacheable(value = "greetings", key = "#id") // value: name of the cache
													// ,key:indexed by the id
													// method parameter
	public Greeting findOne(Long id) {
		counterService.increment("method.invoked.greetingServiceBean.findOne");
		Greeting greeting = greetingRepository.findOne(id);
		return greeting;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	@CachePut(value = "greetings", key = "#result.id")
	public Greeting create(Greeting greeting) {
		if (greeting.getId() != null) {
			return null;
		}
		counterService.increment("method.invoked.greetingServiceBean.create");
		Greeting savedGreeting = greetingRepository.save(greeting);
		// This is dummy check to see how transaction works!!!!
		/*if (savedGreeting.getId() == 4L) {
			throw new RuntimeException();
		}*/
		return savedGreeting;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	@CachePut(value="greetings",key="#greeting.id")
	public Greeting update(Greeting greeting) {
		counterService.increment("method.invoked.greetingServiceBean.update");
		Greeting greetingPersisted = greetingRepository.findOne(greeting.getId());
		if (greetingPersisted.getId() == null) {
			return null;
		}
		Greeting updatedGreeting = greetingRepository.save(greeting);
		return updatedGreeting;
	}

	@Override
	@CacheEvict(value="greetings", key="#id") //remove cached items
	public void remove(Long id) {
		counterService.increment("method.invoked.greetingServiceBean.remove");
		greetingRepository.delete(id);

	}

	@Override
	@CacheEvict(value="greetings", allEntries=true) //evicts all cache entries with the value provided
	public void evictCache() {
		
	}

}
