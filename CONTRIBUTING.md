Contribution Guidelines
===

These are preliminary guidelines.

Code
---

* Target Java 6
* Unix line endings (\n)
  * You can configure Git to convert for you
  * for Windows: `git config --global core.autocrlf true`
* Style:
  * __MUST__ follow [Oracle guidlelines](http://www.oracle.com/technetwork/java/javase/documentation/codeconvtoc-136057.html&sa=D&sntz=1&usg=AFQjCNHTxMh5uL6sH4szb8ShDwYs5Ba_dQ)
  * __MUST__ use 4 spaces for tabs
* Javadocs:
  * __MUST__ properly provide Javadocs for public facing API (not necessary for concrete implementations)
  * __AVOID__ using @author; that’s what git blame is for.
* Naming convention:
  * Use “ID” rather than “id” in method names (getID())
* Annotation usage:
  * All methods that return `null` __MUST__ be annotated with `@Nullable` (from `javax.*`)
  * All method parameters that accept `null` __MUST__ be annotated with `@Nullable`
  * Use [Google Preconditions](https://code.google.com/p/guava-libraries/wiki/PreconditionsExplained), especially as a guard against unexpected nulls, in all public facing APIs
checkNotNull(param);
  * All implemented or overridden methods __MUST__ be annotated with `@Override`

Pull Requests
---
We plan on creating a “pre-pull request” procedure so we can work with the community __before__ they spend their time on something that we don’t feel fits in Sponge (or in case the person writing the PR is a bit over their head).

However, going through this “pre-PR” procedure is __optional__.
* For smaller changes, commits should be squashed (combined into one).
* For large changes, commits can be left separate.
* Clearly, you will have to mention what is being added / changed / removed with sufficient detail.

Versioning
---
* We track Minecraft versions (1.5, 1.6, 1.7.2, 1.7.10)
* We also have our own internal version which tracks the state of the API 
  * Semantic versioning (ex. 1.0.0, 1.10, 1.2.1, 2.0.1, etc)

Git
---
* We are not signing off on commits.
* Proper commit messages especially for larger changes. Changes should be described and explained, stuff like “fixed some issues with bla bla” should be avoided.
* Use imperative mood for commit messages (i.e. “Fix x and y.” and not “Fixed x and y.”)

Branches
---
* Current Minecraft version + current internal version: __master__
* Older MC version + any major internal: __release/$mc-$internal__
* Features in development: __feature/$feature__
  * Merge into master or release/ when we are done
  * A feature is any big change that you think benefits from input from others
* Hotfixes: __hotfix/$hotfix__
