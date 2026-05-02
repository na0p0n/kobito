import NextAuth, { type Session } from "next-auth";
import type { AdapterUser } from "@auth/core/adapters";
import GitHub from "next-auth/providers/github";
import PostgresAdapter from "@auth/pg-adapter";
import { Pool } from "pg";

const pool = new Pool({ connectionString: process.env.DATABASE_URL });

export const { handlers, auth, signIn, signOut } = NextAuth({
  adapter: PostgresAdapter(pool),
  providers: [
    GitHub({
      clientId: process.env.GITHUB_CLIENT_ID!,
      clientSecret: process.env.GITHUB_CLIENT_SECRET!,
      authorization: { params: { scope: "read:user user:email repo" } },
    }),
  ],
  callbacks: {
    async session({ session, user, account }) {
      session.user.id = user.id;
      let accessToken = account?.access_token;
      if (!accessToken) {
        const result = await pool.query(
          "SELECT access_token FROM accounts WHERE user_id = $1 AND provider = $2",
          [user.id, "github"]
        );
        accessToken = result.rows[0]?.access_token ?? undefined;
      }
      if (accessToken) {
        (session as Session & { accessToken: string }).accessToken = accessToken;
      }
      return session;
    },
  },
});
