package com.sellsphere.admin.transaction;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int TRANSACTION_PER_PAGE = 10;
    private final PaymentIntentRepository transactionRepository;


    public void listEntities(PagingAndSortingHelper helper, Integer pageNum) {
        helper.listEntities(pageNum, TRANSACTION_PER_PAGE, transactionRepository);
    }

}
