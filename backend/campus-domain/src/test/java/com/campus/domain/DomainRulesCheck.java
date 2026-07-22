package com.campus.domain;

import com.campus.domain.activity.Activity;
import com.campus.domain.card.CampusCard;
import com.campus.domain.location.RoutePlanner;
import com.campus.domain.repair.RepairOrder;
import com.campus.domain.score.ScorePolicy;

import java.math.BigDecimal;
import java.util.List;

public class DomainRulesCheck {
    public static void main(String[] args) {
        CampusCard card = new CampusCard(BigDecimal.TEN, CampusCard.Status.NORMAL);
        card.recharge(BigDecimal.valueOf(5));
        assert card.balance().equals(BigDecimal.valueOf(15));
        card.reportLoss();
        assert card.status() == CampusCard.Status.LOST;

        Activity activity = new Activity(2, 1);
        activity.register();
        assert activity.participants() == 2;

        RepairOrder repair = new RepairOrder(RepairOrder.Status.COMPLETED);
        repair.evaluate(5, "处理及时");
        assert repair.status() == RepairOrder.Status.EVALUATED;

        ScorePolicy.Statistics scores = ScorePolicy.calculate(List.of(
                new ScorePolicy.CourseScore(BigDecimal.valueOf(4), BigDecimal.valueOf(90)),
                new ScorePolicy.CourseScore(BigDecimal.valueOf(2), BigDecimal.valueOf(80))));
        assert scores.gpa().equals(new BigDecimal("3.67"));

        assert RoutePlanner.walking(23.1, 113.3, 23.1, 113.3).distance() == 0;
    }
}
