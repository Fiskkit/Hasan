package com.fiskkit.instantEmail;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.HeadlessException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import twitter4j.Status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)

public class ControllerTest {
	@Autowired
	FiskController controller;

	private static Logger LOG = LoggerFactory.getLogger(ControllerTest.class);
	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void twitter() throws Exception {
		restTemplate.getForObject("http://localhost" + port + "/tweet/Ubgz4zDZvR", Status.class);
		try {
			JOptionPane.showMessageDialog(null,
					"Now check https://twitter.com/allfisks for the notification to @hdiwan");
		} catch (HeadlessException e) {
			LOG.warn("Now check https://twitter.com/allfisks for the notification to @hdiwan");
		}
		assertThat(1);
	}

	@Test
	public void isCreatedProperly() throws Exception {
		assertThat(controller).isNotNull();
	}

	@Test
	public void userValidTest() {
		assertThat((restTemplate.getForObject("http://localhost:" + port + "/valid?subscription=1sjs9hvQ5tmUbX2I1Z",
				String.class) == "true"));

	}

	@Test
	public void expiredSubscription() {
		assertThat(restTemplate.getForObject("http://localhost:" + port + "/valid?subscription=cbdemo_dave-sub2",
				String.class) == "false");
	}

	@Test
	public void readabilityOfText() {
		String text = "the quick brown fox jumped over the lazy dog.";
		assertThat(restTemplate.postForObject("http://localhost:" + port + "/readability", text, Double.class) < 1.00);
	}

	@Test
	public void statistics() {
		String text = "the quick brown fox jumped over the lazy dog.";
		@SuppressWarnings("unchecked")
		Map<String, String> stats = restTemplate.postForObject("http://localhost:" + port + "/analyze", text,
				Map.class);
		Set<String> expectedKeys = new HashSet<>();
		expectedKeys.add("wordCount");
		expectedKeys.add("averageWordLength");
		expectedKeys.add("mostCommonWords");
		assertThat(stats.keySet().containsAll(expectedKeys));
	}

	@Test
	public void getText() {
		String actual = restTemplate.getForObject("http://localhost:" + port
				+ "/text?url=https%3A%2F%2Fwww.reddit.com%2Fr%2Fhelp%2Fcomments%2F6lqm79%2Fis_there_a_way_to_determine_my_total_post_count%2Fdk9nqro%2F",
				String.class);
		String expected = "cruyff8 comments on Is there a way to determine my total post count per subreddit? jump to content my subreddits edit subscriptions popular -all -random |  AskReddit -funny -worldnews -todayilearned -pics -videos -gifs -aww -news -movies -gaming -mildlyinteresting -television -Showerthoughts -Jokes -sports -OldSchoolCool -nottheonion -IAmA -tifu -photoshopbattles -explainlikeimfive -science -LifeProTips -personalfinance -TwoXChromosomes -EarthPorn -food -Futurology -space -Music -WritingPrompts -UpliftingNews -Art -dataisbeautiful -nosleep -GetMotivated -Documentaries -askscience -gadgets -books -announcements -creepy -DIY -history -listentothis -philosophy -InternetIsBeautiful -blogmore »  help comments Want to join? Log in or sign up in seconds.| English limit my search to r/help use the following search parameters to narrow your results: subreddit:subreddit find submissions in \"subreddit\" author:username find submissions by \"username\" site:example.com find submissions from \"example.com\" url:text search for \"text\" in url selftext:text search for \"text\" in self post contents self:yes (or self:no) include (or exclude) self posts nsfw:yes (or nsfw:no) include (or exclude) results marked as NSFW e.g. subreddit:aww site:imgur.com dog see the search faq for details. advanced search: by author, subreddit... this post was submitted on 07 Jul 2017 1 point (100% upvoted) shortlink: remember mereset password login Ask a question about Reddit helpsubscribeunsubscribe9,877 readers 308 users here now For your questions about reddit only, please. Check the FAQ to see if your question has already been answered. New to reddit? click here! reddit status -- status page for checking site health Are your submissions not showing on reddit? If you are a new user/trying to submit in a reddit you have not submitted to before, please take some time to first participate in that reddit (browse, upvote content/comments etc). If you have tried the above and your posts still do not show, please contact a moderator in the reddit where you're having problems. See the FAQ for more information. For other questions not specific to reddit, try: /r/AskReddit /r/Advice /r/needadvice /r/techsupport /r/relationship_advice Other helpful subreddits: /r/Bugs - If you have found a possible bug in reddit. /r/goldbenefits - Check out new gold features and reddit gold partners /r/RedditMobile - Official reddit mobile apps /r/MobileWeb - Reddit mobile website /r/Enhancement - Reddit Enhancement Suite (RES) from /u/honestbleeps /r/RESissues - For your RES problems /r/redditrequest - Request to moderate an abandoned subreddit or request a subreddit be unbanned /r/adoptareddit - Give away or acquire an unused subreddit. /r/needamod - Need help moderating a reddit? Want to volunteer? /r/AskModerators - for general questions aimed at moderators of reddit. /r/modhelp - Help for questions about moderation. /r/csshelp - subreddit style help /r/modsupport - a point of contact for moderators to discuss issues with reddit admins, mostly about mod tools /r/modclub A place for mods of communities with 100 or more users to hang out /r/i18n - help translate reddit! /r/ideasfortheadmins - suggestions to improve Reddit /r/FindAReddit - Looking for a subreddit on a particular topic but don't know where to start? /r/FindASubreddit - Looking for a subreddit on a particular topic but don't know where to start? /r/aboutreddit - for submissions about Reddit. /r/TheoryOfReddit - to discuss Reddit. The lightbulb icons next to usernames indicate people who have demonstrated a history of providing useful and helpful answers in this subreddit. Be sure to check the information in the sidebar and the FAQ! Join us in IRC #reddit-help on irc.snoonet.org a community for 9 years message the moderators MODERATORS krispykrackers qgyh2 ytwang davidreiss666Helper Monkey Skuld redtabooadmin Raerth sodypopadmin 316nutshelper allthefoxeshelper ...and 3 more » discussions in r/help <> X 2 · 1 comment Can't upload a picture · 4 comments r/TestSubredditPleaseIgnore can't be created 1 · 1 comment How can i set user flair in reddit is fünf Android APP? 3 · 2 comments My karma hasn't updated for the last 2 days. I have had multiple posts with a lot of upvotes but my karma total is staying the same. Is there a way to fix this? 1 · 7 comments Why do some subreddits always have a lot of members browsing the sub at any given time, yet the subs are very inactive? 3 · 2 comments Is the profile beta actually limited or is it based on my karma? 1 · 3 comments collapse tread 1 · 2 comments Someone is impersonating my friend, as in deliberately. How do I get his account removed 1 · 1 comment My karma count is stuck at 9,222 1 · 4 comments karma should have inflation, what when you dont have interest in a sub anymore you know? any way to transfer post karma? or delete karma for a specific sub so it doesn't show up top on your profile? or donate karma? 0 1 2 Is there a way to determine my total post count per subreddit? (self.help) submitted 9 days ago by theFloodShark 13 comments share loading... sorted by: best topnewcontroversialoldrandomq&alive (beta) you are viewing a single comment's thread. view the rest of the comments → [–]cruyff8 0 points1 point2 points 16 hours ago* (0 children) Loop until the length is less than your limit. I'm not on PC till later today, else I'd provide the actual incantation. EDIT: Now that I am at a computer, the incantation you need is limit=None, as of last August, anyway. permalink embed save parent report give gold reply about blog about source code advertise careers help site rules FAQ wiki reddiquette mod guidelines contact us apps & tools Reddit for iPhone Reddit for Android mobile website buttons <3 reddit gold redditgifts Use of this site constitutes acceptance of our User Agreement and Privacy Policy. © 2017 reddit inc. All rights reserved. REDDIT and the ALIEN Logo are registered trademarks of reddit inc. π Rendered by ";
		assertThat(actual.contains(expected));
	}

	@Test
	public void potentialUrls() {
		assertThat(restTemplate.getForObject("http://localhost:" + port + "/url?url=%3A%2F%2F",
				Boolean.class) == Boolean.FALSE);
		assertThat(restTemplate.getForObject("http://localhost:" + port + "/url?url=http%3A%2F%2Fwww.google.co.uk%2F",
				Boolean.class) == Boolean.TRUE);

	}
}
